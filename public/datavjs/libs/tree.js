/*global EventProxy, d3, Raphael, $ */
define(function (require, exports, module) {
    var DataV = require('datav');
    var theme = DataV.Themes;

    var Tree = DataV.extend(DataV.Chart, {
        initialize: function (container, options) {
            this.type = "Tree";
            this.container = container;
            this.defaults = {};

            this.addlink = {};

            // Properties
            this.treeDepth = 0;
            this.font = {};

            // Canvas
            this.defaults.width = 750;
            this.defaults.height = 760;
            this.defaults.deep = 180;
            this.defaults.radius = 15;

            this.setOptions(options);
            this.emitter = new EventProxy();
            this.createCanvas();

            //this.msgDivArr = [];
        }
    });

    Tree.prototype.on = function (eventName, callback) {
        this.emitter.on(eventName, callback);
    };

    Tree.prototype.setOptions = function (options) {
        var prop;
        if (options) {
            for (prop in options) {
                if (options.hasOwnProperty(prop)) {
                    this.defaults[prop] = options[prop];
                }
            }
        }
    };

    Tree.prototype.hierarchyTableToJson = function (table) {
        if (table[0][0] === "ID") {
            table = table.slice(1);
        }

        var rootID;
        var hierarchy = {};
        var addlink = {}; //for multi-fathernode

        table.forEach(function (d, i) {
            if (d[0] === "") {
                throw new Error("ID can not be empty(line:" + (i + 1) + ").");
            }
            if (!d[3]) {
                if (rootID) {
                    throw new Error("2 or more lines have an empty parentID(line:" + (i + 1) + ").");
                } else {
                    rootID = d[0];
                }
            }
            if (hierarchy[d[0]]) {
                throw new Error("2 or more lines have same ID: " + d[0] + "(line:" + (i + 1) + ").");
            }

            var value = "";
            var j, length;
            if (d.length > 4) {
                for (j = 4, length = d.length; j < length; j++) {
                    if (j < length - 1) {
                        value = value + d[j] + ",";
                    } else {
                        value = value + d[j];
                    }
                }
            }
            hierarchy[d[0]] = {name: d[1], size: d[2], child: [], id: d[0], value: value};
        });
        if (!rootID) {
            throw new Error("No root node defined.");
        }
        table.forEach(function (d, i) {
            if (d[3]) {
                var record;
                var ids = d[3].split('.');
                if (ids.length === 1) {
                    record = hierarchy[d[3]];
                    record.child.push(d[0]);
                } else {
                    record = hierarchy[ids[0]];
                     record.child.push(d[0]);
                    addlink[d[0]] = {child: [], path: [], pnode: []};

                    var j, length;
                    for (j = 1, length = ids.length; j < length;  j++) {
                        addlink[d[0]].child.push(ids[j]);
                    }
                }
                if (!record) {
                    throw new Error("Can not find parent with ID " + d[3] + "(line:" + (i + 1) + ").");
                }
            }
        });

        this.addlink = addlink;

        var recurse = function (rootID) {
            var record = hierarchy[rootID];
            if (record.child.length === 0) {
                if (isNaN(parseFloat(record.size))) {
                    throw new Error("Leaf node's size is not a number(ID:" + (rootID + 1) + ").");
                } else {
                    return {name: record.name, 
                        size: record.size, 
                        num: record.id, 
                        children: null, 
                        draw: false, 
                        value: record.value};
                }
            } else {
                var childNode = [];
                record.child.forEach(function (d) {
                    childNode.push(recurse(d));
                });
                return {name: record.name, children: childNode, num: record.id, draw: false, value: record.value};
            }
        };

        return recurse(rootID);
    };

    Tree.prototype.setSource = function (source) {
        var conf = this.defaults;

        this.rawData = this.hierarchyTableToJson(source);
        this.source = this.remapSource(source);
        
        this.source.x0 = conf.width / 2;
        this.source.y0 = conf.radius * 10;

        this.source.children.forEach(function collapse(d) {
            if (d.children) {
                // d._children = d.children;
                // d._children.forEach(collapse);
                // d.children = null;
                d._children = null;
                d.children.forEach(collapse);
            }
        });
    };

    Tree.prototype.remapSource = function (data) {
        return this.hierarchyTableToJson(data);
        // return data;
    };

    Tree.prototype.layout = function () {
        var conf = this.defaults;
        var tree = d3.layout.tree()
            .size([conf.width, conf.height]);

        this.nodesData = tree.nodes(this.source);

        var treedepth = 0;
        var id = 0;

        this.nodesData.forEach(function (d) {
            if (d.depth > treedepth) {
                treedepth = d.depth;
            }
        });

        this.treeDepth = treedepth;
        conf.deep = conf.height / (treedepth + 1);

        this.nodesData.forEach(function (d) {
            d.y = conf.radius * 3 + d.depth * conf.deep;
            d.id = id;
            id++;
        });
    };

    Tree.prototype.getColor = function () {
        //var conf = this.defaults;
        //var treedepth = this.treeDepth;

        var colorMatrix = DataV.getColor();
        var color;
        if (colorMatrix.length > 1 && colorMatrix[0].length > 1) {
            color = [colorMatrix[0][0], colorMatrix[1][0]];
        } else {
            color = colorMatrix[0];
        }
        //var colorRow_Num = colorRow.length - 1;

        return DataV.gradientColor(color, "special");
        
        // return function(d){
        //     var color;

        //     if ((treedepth * 2 - 1 )> colorRow_Num) {
        //         if (d > colorRow_Num) {
        //             color = colorRow[colorRow_Num];
        //         } else {
        //             color = colorRow[d];
        //         }
        //     } else {
        //         if (d == 0) {
        //             color = colorRow[0];
        //         } else {
        //             color = colorRow[d * 2 - 1];
        //         }
        //     }

        //     return color;
        // }
    };

    // Tree.prototype.getFont = function () {
    //     //var conf = this.defaults;

    //     return DataV.getFont();
    // };

    Tree.prototype.createCanvas = function () {
        var conf = this.defaults;
        this.canvas = new Raphael(this.container, conf.width, conf.height);
        this.canvasF = document.getElementById(this.container);
        canvasStyle = this.canvasF.style;
        canvasStyle.position = "relative";
        this.floatTag = DataV.FloatTag()(this.canvasF);

        this.floatTag.css({"visibility": "hidden"});

        this.DOMNode = $(this.canvas.canvas);
        var that = this;
        this.DOMNode.click(function (event) {
            that.emitter.trigger("click", event);
        });
        this.DOMNode.dblclick(function (event) {
            that.emitter.trigger("dblclick", event);
        });

        var mousewheel = document.all ? "mousewheel" : "DOMMouseScroll";  
        this.DOMNode.bind(mousewheel, function (event) {
            that.emitter.trigger("mousewheel", event);
        });

        this.DOMNode.bind("contextmenu", function (event) {
            that.emitter.trigger("contextmenu", event);
        });

        this.DOMNode.delegate("circle", "click", function (event) {
            that.emitter.trigger("circle_click", event);
        });

        this.DOMNode.delegate("circle", "mouseover", function (event) {
            that.emitter.trigger("circle_mouseover", event);
        });

        this.DOMNode.delegate("circle", "mouseout", function (event) {
            that.emitter.trigger("circle_mouseout", event);
        });

    };

    Tree.prototype.zoom = function (d) {
        var multiple = 2;

        if (d !== null) {
            multiple = d;
        }

        var conf = this.defaults;

        conf.width = conf.width * multiple;

        if (conf.height <= this.treeDepth * conf.deep) {
            conf.height = conf.height * multiple;
        }

        //this.createCanvas();
        this.canvas.setSize(conf.width, conf.height);
        this.canvas.setViewBox(0, 0, conf.width, 800);
        this.defaults = conf;

        this.render();
    };


    Tree.prototype.getLinkPath = function (fx, fy, tx, ty) {
        var conf = this.defaults;

        var c1x = fx;
        var c1y = fy + (ty - fy) / 2;
        var c2x = tx;
        var c2y = ty - (ty - fy) / 2;

        var link_path = [["M", fx, fy + conf.radius], 
            ["C", c1x, c1y, c2x, c2y, tx, ty - conf.radius]];

        return link_path;    
    };

    Tree.prototype.generatePaths = function () {
        var canvas = this.canvas;
        var source = this.source;
        var conf = this.defaults;
        var radius = conf.radius;
        //canvas.clear();
        var color = this.getColor();
        // var font = this.getFont();
        var font_family = '微软雅黑';
        var font_size = 8;
        var treedepth = this.treeDepth;
        var nodesData = this.nodesData;

        var n = 0;

        var addlink = this.addlink;
        var node;
        var num = 0;

        var nodes = canvas.set();
        var path = [];
        var textpath = [];
        
        var tree = this;
        var nodeupdate = function () {
            tree.update(this.data("num"));
        };

        $("#" + this.container).append(this.floatTag);

        var i, nodesLength;
        for (i = 0, nodesLength = nodesData.length; i < nodesLength;  i++) {
            var d =  nodesData[i];
            var parent = d.parent;

            if (addlink[d.num]) {
                var j, k, childLength;
                for (j = 0, childLength = addlink[d.num].child.length; j < childLength; j++) {
                    for (k = 0; k < nodesLength;  k++) {
                        if (nodesData[k].num === addlink[d.num].child[j]) {
                            addlink[d.num].pnode[j] = k;
                            addlink[d.num].path[j] = canvas.path()
                                .attr({ stroke:"#939598", "stroke-width":0.5});
                        }
                    }
                } 
            }

            var startX;
            var startY;

            if (parent && d.draw) {
                startX = parent.x;
                startY = parent.y;
            } else {
                startX = d.x;
                startY = d.y;
            }
            if (parent) {
                //path.push(canvas.path().attr({stroke:"#939598", "stroke-width":0.5}));
                if(d.value == "flag"){
                    path.push(canvas.path().attr({stroke:"#ffffff", "stroke-width":0.0001}));
                }else if(d.value == "flagText"){ //这个不画
                    //path.push(canvas.path().attr({stroke:"#ffffff", "stroke-width":0.0001}));
                }else{
                    path.push(canvas.path().attr({stroke:"#939598", "stroke-width":0.5}));
                }
            }

            if(d.value != "flagText"){
                nodes.push(
                    canvas.circle(startX, startY, radius)
                        .attr({fill: color(d.depth / treedepth),
                            stroke:  "#ffffff",
                            "stroke-width": 1,
                            "fill-opacity": 0.4,
                            "data": 12})
                        .data("num", i)
                        .animate({cx: d.x, cy: d.y}, 500, "backOut")
                );
            }else{ //画大的文本flag的框
                nodes.push(
                    canvas.rect(startX, startY - 15, 170, 30, 5)
                        .attr({fill: color(d.depth / treedepth),
                            stroke:  "#ffffff",
                            "stroke-width": 1,
                            "fill-opacity": 0.4,
                            "data": 12})
                        .data("num", i)
                        .animate({cx: d.x, cy: d.y}, 500, "backOut")
                );
            }

//            nodes.push(
//                canvas.rect(startX + radius, startY-radius , 2 * radius, 2 * radius)
//            );


//            if (d.value && d.value != "null" && d.value != "flag") {
//                var msgDom = $("<div>").css({"border":"1px", "background-color":"red", "padding":"5px", position:"absolute", left:startX + radius + "px", top:startY - radius + "px"}).text(d.value);
//                this.msgDivArr.push(msgDom);
//                $("#" + this.container).append(msgDom);
//            }

            if (d.children || d._children) {
                nodes[i].click(nodeupdate);
            }

            if (d._children) {
                nodes[i].attr({stroke:  color(d.depth / treedepth), 
                    "stroke-width": radius, 
                    "stroke-opacity": 0.4, 
                    "fill-opacity": 1, 
                    "r": radius / 2});
            }

            if (d.children) {
                textpath.push(canvas.text(d.x, d.y - radius - 7, d.name).attr({'font-size': 12}));
            } else {
                if(d.value == "flag"){
                    textpath.push(canvas.text(d.x, d.y, d.name).attr({'font-size': 20}));
                }else if(d.value == "flagText"){
                    textpath.push(canvas.text(d.x + 80, d.y, d.name).attr({'font-size': 12})); //这里是画文本的
                }else{
                    textpath.push(canvas.text(d.x, d.y + radius + 7, d.name).attr({'font-size': 12}));
                }
            }
        }

        var floatTag = this.floatTag;
        nodes.forEach(function (d, i) {
            $(d.node).attr('value', nodesData[i].value);
            var textY = textpath[i].attr('y');
            var thisradius = d.attr('r');
            var thisstrokewidth = d.attr('stroke-width');
            if (nodesData[i].value != "flag" && nodesData[i].value != "flagText") { //add by wen66, 如果是符号,就不要有鼠标移上去的效果了
                d.mouseover(function () {
                    if (!nodesData[i]._children) {
                        this.animate({r:thisradius + 2, "fill-opacity":0.75}, 100);
                    } else {
                        this.animate({r:thisradius + 2, "stroke-opacity":0.75}, 100);
                    }

                    textpath[i].attr({'font-size':20});

                    if (i > 0) {
                        if (!nodesData[i].children) {
                            textpath[i].animate({'y':textY + 12}, 100, "backOut");
                        } else {
                            textpath[i].animate({'y':textY - 12}, 100, "backOut");
                        }
                    }

                    var getFline = function (node, num) {
                        var parent = node.parent;
                        if (parent) {
                            path[node.id - 1].attr({"stroke-width":4, "stroke-opacity":num});
                            if (num > 0.5) {
                                num = num - 0.1;
                            }
                            getFline(parent, num);
                        }
                    }

                    getFline(nodesData[i], 0.9);

                    var thisparent = nodesData[i].parent;
                    var j, textpathLength;
                    for (j = 0, textpathLength = textpath.length; j < textpathLength; j++) {
                        var parent = nodesData[j].parent;
                        if (parent === thisparent && j !== i) {
                            textpath[j].animate({'fill-opacity':0.4});
                        }
                    }

                    var floagTagStr = nodesData[i].value != "null" ? nodesData[i].value : nodesData[i].name;

                    floatTag.html('<div style = "text-align: center;margin:auto;color:'
                        //+ jqNode.color
                        + "#ffffff"
                        + '">' + floagTagStr + '</div>'
                        );
                    floatTag.css({"visibility":"visible"});
                })
                    .mouseout(function () {
                        floatTag.css({"visibility":"hidden"});
                        if (!nodesData[i]._children) {
                            this.animate({r:thisradius, "fill-opacity":0.4}, 100);
                        } else {
                            this.animate({r:thisradius, "stroke-width":thisstrokewidth, "stroke-opacity":0.4}, 100);
                        }
                        textpath[i].attr({'font-size':12});
                        textpath[i].animate({'y':textY}, 100, "backOut");

                        var getFline = function (node) {
                            var parent = node.parent;
                            if (parent) {
                                path[node.id - 1].attr({"stroke-width":0.5, "stroke-opacity":1});
                                getFline(parent);
                            }
                        }
                        getFline(nodesData[i]);

                        var thisparent = nodesData[i].parent;
                        var j, textpathLength;
                        for (j = 0, textpathLength = textpath.length; j < textpathLength; j++) {
                            var parent = nodesData[j].parent;
                            if (parent === thisparent && j !== i) {
                                textpath[j].animate({'fill-opacity':1});
                            }
                        }
                    });
            }

        });

        nodes.onAnimation(function () {
            var pathNum = 0;
            var i, nodeslength;
            
            for (i = 1, nodeslength = nodes.length; i < nodeslength;  i++) {
                var d = nodes[i];
                var node = nodesData[i];
                var parent = node.parent;
                
                path[pathNum]
                    .attr({path: tree.getLinkPath(parent.x, parent.y, d.attr("cx"), d.attr("cy"))});
                    
                pathNum++;

                if (addlink[node.num]) {
                    var j, k, linkchildLength, nodesLength;
                    for (j = 0, linkchildLength = addlink[node.num].child.length; j < linkchildLength; j++) {
                        for (k = 0, nodesLength = nodesData.length; k < nodesLength;  k++) {
                            var anparent = nodesData[k];
                            if (anparent.num === addlink[node.num].child[j]) {
                                var link_path = tree.getLinkPath(anparent.x, anparent.y, d.attr("cx"), d.attr("cy"));
                                addlink[node.num].path[j].attr({path: link_path});
                            }
                        }
                    } 
                }
            }
        });
        
        this.nodes = nodes;
        this.path = path;
        this.textpath = textpath;
        // this.link_paths = link_paths;
        // this.circle_paths = circle_paths;
        // this.text_paths = text_paths;
    };
    
    Tree.prototype.update = function (i) {
        var source = this.source;
        var conf = this.defaults;

        source.children.forEach(function clearDraw(d) {
            d.draw = false;
            if (d.children) {
                d.children.forEach(clearDraw);
            }
        });

        source.children.forEach(function find(d) {
            if (d.id === i) {
                if (d.children) {
                    d._children = d.children;
                    d.children = null;
                } else {
                    d.children = d._children;
                    if (d.children) {
                        d.children.forEach(function drawn(child) {
                            child.draw = true;
                            if (child.children) {
                                child.children.forEach(drawn);
                            }
                        });
                    }
                    d._children = null;
                }
            } else {
                if (d.children) {
                    d.children.forEach(find);
                }
            }
        });
        this.source = source;
        this.source.x0 = conf.width / 2;
        this.source.y0 = conf.radius * 2;
        this.render();
    };
    


    Tree.prototype.render = function (options) {
        // var st = new Date().getTime();
        if (!this.container) {
            throw new Error("Please specify which node to render.");
        }
        this.canvas.clear();

//        for(var i=0; i<this.msgDivArr.length; i++){
//            this.msgDivArr[i].remove();
//        }
//        this.msgDivArr.length = 0;


        this.setOptions(options);
        this.layout();
        // var st2 = new Date().getTime();

        this.generatePaths();
        // var et = new Date().getTime();
        //this.canvas.renderfix();
    };

    module.exports = Tree;
});
