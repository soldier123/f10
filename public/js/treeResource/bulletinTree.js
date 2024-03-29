var bulletinTree =
    [
        {
            text:"全部公告",
            "code":"I01",
            "pcode":"I01",
            "classfyId":["%"],
            isexpand:false,
            "children":[
                {
                    text:"创业版公司公告",
                    "code":"I02",
                    "pcode":"I02",
                    "classfyId":["010115",
                        "014120",
                        "014122",
                        "014124",
                        "014125",
                        "014127",
                        "019901"]
                },
                {
                    text:"股权分置改革",
                    "code":"I03",
                    "pcode":"I03",
                    "classfyId":["011305"]
                },
                {
                    text:"融资融券",
                    "code":"I04",
                    "pcode":"I04",
                    "classfyId":["%融券%"]
                },
                {
                    text:"定期报告",
                    "code":"I05",
                    "pcode":"I05",
                    "classfyId":[],
                    isexpand:false,
                    "children":[
                        {
                            text:"年报",
                            "code":"I0501",
                            "pcode":"I05",
                            "classfyId":["010301",
                                "01030101",
                                "01030110",
                                "01030120"]
                        },
                        {
                            text:"中报",
                            "code":"I0502",
                            "pcode":"I05",
                            "classfyId":["010303",
                                "01030301",
                                "01030310",
                                "01030320"]
                        },
                        {
                            text:"季报",
                            "code":"I0503",
                            "pcode":"I05",
                            "classfyId":["010305",
                                "01030501",
                                "01030510",
                                "01030520",
                                "010307",
                                "01030701",
                                "01030710",
                                "01030720"]
                        },
                        {
                            text:"业绩快报",
                            "code":"I0504",
                            "pcode":"I05",
                            "classfyId":["01211160"]
                        },
                        {
                            text:"补充更正公告",
                            "code":"I0505",
                            "pcode":"I05",
                            "classfyId":["01030130",
                                "01030140",
                                "01030330",
                                "01030340",
                                "01030530",
                                "01030540",
                                "01030730",
                                "01030740",
                                "0127",
                                "012701",
                                "01270101",
                                "01270110",
                                "012703",
                                "01270301",
                                "01270310",
                                "012799",
                                "01279901",
                                "01279910",
                                "01279920"]
                        },
                        {
                            text:"业绩预告",
                            "code":"I0506",
                            "pcode":"I05",
                            "classfyId":["012111",
                                "01211101",
                                "01211110",
                                "01211115",
                                "01211120",
                                "01211130",
                                "01211140",
                                "01211150"]
                        }
                    ]
                },
                {
                    text:"IPO招股",
                    "code":"I06",
                    "pcode":"I06",
                    isexpand:false,
                    "children":[
                        {
                            text:"招股说明书（申报稿）",
                            "code":"I0601",
                            "pcode":"I06",
                            "classfyId":["010116"]
                        },
                        {
                            text:"发审委公告",
                            "code":"I0602",
                            "pcode":"I06",
                            "classfyId":["010201"]
                        },
                        {
                            text:"招股说明书",
                            "code":"I0603",
                            "pcode":"I06",
                            "classfyId":["010203",
                                "01020301",
                                "01020303",
                                "01020320",
                                "01020321"]
                        },
                        {
                            text:"发行公告书",
                            "code":"I0604",
                            "pcode":"I06",
                            "classfyId":["010205",
                                "01020501",
                                "01020510",
                                "01020520",
                                "010207",
                                "010209",
                                "01020901",
                                "01020910",
                                "01020920",
                                "01020930",
                                "010211",
                                "01021101",
                                "01021110",
                                "01021120"]
                        },
                        {
                            text:"上市公告书",
                            "code":"I0605",
                            "pcode":"I06",
                            "classfyId":["010213"]
                        }
                    ]
                },
                {
                    text:"配股",
                    "code":"I07",
                    "pcode":"I07",
                    isexpand:false,
                    "children":[
                        {
                            text:"配股说明书",
                            "code":"I0701",
                            "pcode":"I07",
                            "classfyId":["010505"]
                        },
                        {
                            text:"配股公告",
                            "code":"I0702",
                            "pcode":"I07",
                            "classfyId":["010511", "010507"]
                        },
                        {
                            text:"上市公告书",
                            "code":"I0703",
                            "pcode":"I07",
                            "classfyId":["010509"]
                        },
                        {
                            text:"配股预案",
                            "code":"I0704",
                            "pcode":"I07",
                            "classfyId":["010501",
                                "01050101",
                                "01050110",
                                "01050120"]
                        },
                        {
                            text:"配股获准公告",
                            "code":"I0705",
                            "pcode":"I07",
                            "classfyId":["010503"]
                        }
                    ]
                },
                {
                    text:"增发",
                    "code":"I08",
                    "pcode":"I08",
                    isexpand:false,
                    "children":[
                        {
                            text:"增发说明书",
                            "code":"I0801",
                            "pcode":"I08",
                            "classfyId":["010705"]
                        },
                        {
                            text:"发行公告",
                            "code":"I0802",
                            "pcode":"I08",
                            "classfyId":["010707",
                                "01070701",
                                "01070710",
                                "01070720",
                                "010709",
                                "010711",
                                "01071101",
                                "01071103",
                                "01071110"]
                        },
                        {
                            text:"上市公告书",
                            "code":"I0803",
                            "pcode":"I08",
                            "classfyId":["010713",
                                "01071301"]
                        },
                        {
                            text:"增发预案",
                            "code":"I0804",
                            "pcode":"I08",
                            "classfyId":["010701",
                                "01070101",
                                "01070110",
                                "01070120"]
                        },
                        {
                            text:"增发获准",
                            "code":"I0805",
                            "pcode":"I08",
                            "classfyId":["010703"]
                        }
                    ]
                },
                {
                    text:"分红",
                    "code":"I09",
                    "pcode":"I09",
                    isexpand:false,
                    "children":[
                        {
                            text:"分配预案",
                            "code":"I0901",
                            "pcode":"I09",
                            "classfyId":["01130101",
                                "01130110"]
                        },
                        {
                            text:"实施公告",
                            "code":"I0902",
                            "pcode":"I09",
                            "classfyId":["01130120"]
                        }
                    ]
                },
                {
                    text:"股权变化",
                    "code":"I10",
                    "pcode":"I10",
                    isexpand:false,
                    "children":[
                        {
                            text:"股权收购",
                            "code":"I1001",
                            "pcode":"I10",
                            "classfyId":["011501",
                                "01150101",
                                "01150110",
                                "01150120",
                                "01150130",
                                "01150140",
                                "01150150",
                                "01150160",
                                "01150199"]
                        },
                        {
                            text:"过户",
                            "code":"I1002",
                            "pcode":"I10",
                            "classfyId":["01150730"]
                        },
                        {
                            text:"托管",
                            "code":"I1003",
                            "pcode":"I10",
                            "classfyId":["01150710"]
                        },
                        {
                            text:"增持减持",
                            "code":"I1004",
                            "pcode":"I10",
                            "classfyId":["01150140"]
                        },
                        {
                            text:"质押冻结",
                            "code":"I1005",
                            "pcode":"I10",
                            "classfyId":["012307",
                                "01230701",
                                "01230710",
                                "01230720",
                                "01230730"]
                        },
                        {
                            text:"要约收购",
                            "code":"I1006",
                            "pcode":"I10",
                            "classfyId":["011505",
                                "01150501",
                                "01150510",
                                "01150520",
                                "01150530",
                                "01150540",
                                "01150550"]
                        },
                        {
                            text:"股权拍卖",
                            "code":"I1007",
                            "pcode":"I10",
                            "classfyId":["01150150"]
                        },
                        {
                            text:"换股",
                            "code":"I1008",
                            "pcode":"I10",
                            "classfyId":["012323",
                                "01232301",
                                "01232310"]
                        },
                        {
                            text:"债转股",
                            "code":"I1009",
                            "pcode":"I10",
                            "classfyId":["010915",
                                "01091501",
                                "01091510",
                                "01091520",
                                "01091530",
                                "01091540"]
                        },
                        {
                            text:"股权变动进展",
                            "code":"I1010",
                            "pcode":"I10",
                            "classfyId":["011507",
                                "01150701",
                                "01150710",
                                "01150720",
                                "01150730",
                                "01150740"]
                        },
                        {
                            text:"股东更名",
                            "code":"I1011",
                            "pcode":"I10",
                            "classfyId":["%股东更名%"]
                        },
                        {
                            text:"股权转让",
                            "code":"I1012",
                            "pcode":"I10",
                            "classfyId":["011501",
                                "01150101",
                                "01150110",
                                "01150120",
                                "01150130",
                                "01150140",
                                "01150150",
                                "01150160",
                                "01150199"]
                        }
                    ]
                },
                {
                    text:"股本变化",
                    "code":"I11",
                    "pcode":"I11",
                    isexpand:false,
                    "children":[
                        {
                            text:"增发",
                            "code":"I1101",
                            "pcode":"I11",
                            "classfyId":["010713",
                                "01071301"]
                        },
                        {
                            text:"配股",
                            "code":"I1102",
                            "pcode":"I11",
                            "classfyId":["010509"]
                        },
                        {
                            text:"送股",
                            "code":"I1103",
                            "pcode":"I11",
                            "classfyId":["01130120"]
                        },
                        {
                            text:"转增股本",
                            "code":"I1104",
                            "pcode":"I11",
                            "classfyId":["01130120"]
                        },
                        {
                            text:"职工股上市",
                            "code":"I1105",
                            "pcode":"I11",
                            "classfyId":["01130301"]
                        }

                    ]
                },
                {
                    text:"上市资格",
                    "code":"I12",
                    "pcode":"I12",
                    isexpand:false,
                    "children":[
                        {
                            text:"特别处理",
                            "code":"I1201",
                            "pcode":"I12",
                            "classfyId":["012501",
                                "01250101",
                                "01250110",
                                "01250120"]
                        },
                        {
                            text:"暂停上市",
                            "code":"I1202",
                            "pcode":"I12",
                            "classfyId":["012503",
                                "01250301",
                                "01250310",
                                "01250320",
                                "01250330",
                                "01250340"]
                        },
                        {
                            text:"恢复上市",
                            "code":"I1203",
                            "pcode":"I12",
                            "classfyId":["012505",
                                "01250501",
                                "01250510",
                                "01250520",
                                "01250530"]
                        },
                        {
                            text:"终止上市",
                            "code":"I1204",
                            "pcode":"I12",
                            "classfyId":["012507",
                                "01250701",
                                "01250710"]
                        }

                    ]
                },
                {
                    text:"资本运作",
                    "code":"I13",
                    "pcode":"I13",
                    isexpand:false,
                    "children":[
                        {
                            text:"收购兼并",
                            "code":"I1301",
                            "pcode":"I13",
                            "classfyId":["011701",
                                "01170101",
                                "01170110",
                                "01170120"]
                        },
                        {
                            text:"控股参股",
                            "code":"I1302",
                            "pcode":"I13",
                            "classfyId":['%']
                        },
                        {
                            text:"资产重组",
                            "code":"I1303",
                            "pcode":"I13",
                            "classfyId":["011703",
                                "01170301",
                                "01170310",
                                "01170320"]
                        },
                        {
                            text:"资产置换",
                            "code":"I1304",
                            "pcode":"I13",
                            "classfyId":["011702"]
                        }
                    ]
                },
                {
                    text:"重大事项",
                    "code":"I14",
                    "pcode":"I14",
                    isexpand:false,
                    "children":[

                        {
                            text:"关联交易",
                            "code":"I1401",
                            "pcode":"I14",
                            "classfyId":["011719",
                                "01171901",
                                "01171910",
                                "01171920",
                                "01171930"]
                        },
                        {
                            text:"违纪违规",
                            "code":"I1402",
                            "pcode":"I14",
                            "classfyId":["012317",
                                "01231701",
                                "01231710",
                                "01231720",
                                "01231730",
                                "01231740",
                                "01231750",
                                "01231760",
                                "01231770",
                                "01231780",
                                "01231790"]
                        },
                        {
                            text:"债权债务",
                            "code":"I1403",
                            "pcode":"I14",
                            "classfyId":["01210920",
                                "01210930",
                                "01231330"]
                        },
                        {
                            text:"担保事项",
                            "code":"I1404",
                            "pcode":"I14",
                            "classfyId":["011711",
                                "01171101",
                                "01171110",
                                "01171120",
                                "01171130"]
                        },
                        {
                            text:"代办转让",
                            "code":"I1405",
                            "pcode":"I14",
                            "classfyId":["%"]
                        },
                        {
                            text:"股权激励",
                            "code":"I1406",
                            "pcode":"I14",
                            "classfyId":["012325"]
                        },
                        {
                            text:"法律纠纷",
                            "code":"I1407",
                            "pcode":"I14",
                            "classfyId":["012309",
                                "01230901",
                                "01230910",
                                "01230920",
                                "01230930",
                                "012311",
                                "01231101",
                                "01231110",
                                "01231120",
                                "01231130"]
                        },
                        {
                            text:"破产清算",
                            "code":"I1408",
                            "pcode":"I14",
                            "classfyId":["012319",
                                "01231901",
                                "01231910",
                                "01231920",
                                "01231930",
                                "01231940",
                                "01231950",
                                "01231960",
                                "01231970"]
                        },
                        {
                            text:"重大合同",
                            "code":"I1409",
                            "pcode":"I14",
                            "classfyId":["012327"]
                        },
                        {
                            text:"重大损失",
                            "code":"I1410",
                            "pcode":"I14",
                            "classfyId":["012313",
                                "01231301",
                                "01231310",
                                "01231320",
                                "01231330"]
                        }
                    ]
                },
                {
                    text:"公司经营",
                    "code":"I15",
                    "pcode":"I15",
                    isexpand:false,
                    "children":[
                        {
                            text:"委托/受托经营",
                            "code":"I1501",
                            "pcode":"I15",
                            "classfyId":["011707",
                                "01170701",
                                "01170710"]
                        },
                        {
                            text:"承包租赁",
                            "code":"I1502",
                            "pcode":"I15",
                            "classfyId":["01170720",
                                "01170730"]
                        },
                        {
                            text:"财务资助/受助",
                            "code":"I1503",
                            "pcode":"I15",
                            "classfyId":["011713",
                                "01171301",
                                "01171310"]
                        },
                        {
                            text:"委托理财",
                            "code":"I1504",
                            "pcode":"I15",
                            "classfyId":["01170530"]
                        },
                        {
                            text:"对外投资",
                            "code":"I1505",
                            "pcode":"I15",
                            "classfyId":["011705",
                                "01170501",
                                "01170510",
                                "01170520"]
                        },
                        {
                            text:"公司减资分立",
                            "code":"I1506",
                            "pcode":"I15",
                            "classfyId":["011513",
                                "01151301",
                                "01151310",
                                "01151320",
                                "01151330"]
                        },
                        {
                            text:"政策影响",
                            "code":"I1507",
                            "pcode":"I15",
                            "classfyId":["012305",
                                "01230501",
                                "01230510",
                                "01230520",
                                "01230530",
                                "01230540",
                                "01230550",
                                "01230560",
                                "01230599"]
                        }
                    ]
                },
                {
                    text:"提示公告",
                    "code":"I16",
                    "pcode":"I16",
                    isexpand:false,
                    "children":[
                        {
                            text:"公司资料变动",
                            "code":"I1601",
                            "pcode":"I16",
                            "classfyId":["012301",
                                "01230101",
                                "01230103",
                                "01230110",
                                "01230120",
                                "01230130",
                                "01230140",
                                "01230150",
                                "01230160",
                                "01230170",
                                "0123017001",
                                "01230180",
                                "01230199"]
                        },
                        {
                            text:"人事变动",
                            "code":"I1602",
                            "pcode":"I16",
                            "classfyId":["012303",
                                "01230301",
                                "01230310",
                                "01230315",
                                "01230320",
                                "01230330",
                                "01230340"]
                        },
                        {
                            text:"高管人员持股变更",
                            "code":"I1603",
                            "pcode":"I16",
                            "classfyId":["01230350"]
                        },
                        {
                            text:"停牌提示",
                            "code":"I1604",
                            "pcode":"I16",
                            "classfyId":["01250310",
                                "014123",
                                "014124",
                                "014101",
                                "014102"]
                        },
                        {
                            text:"涨跌异动",
                            "code":"I1605",
                            "pcode":"I16",
                            "classfyId":["012103",
                                "01210301",
                                "01210310",
                                "01210320",
                                "01210330",
                                "01210340",
                                "01210399"]
                        },
                        {
                            text:"大宗交易",
                            "code":"I1606",
                            "pcode":"I16",
                            "classfyId":["014105"]
                        },
                        {
                            text:"风险提示",
                            "code":"I1607",
                            "pcode":"I16",
                            "classfyId":["014129",
                                "012105",
                                "012107",
                                "012109",
                                "01210901",
                                "01210910",
                                "01210920",
                                "01210930",
                                "01210940",
                                "01210950",
                                "01210960",
                                "01210970",
                                "01210980",
                                "01210990",
                                "012103",
                                "01210301",
                                "01210310",
                                "01210320",
                                "01210330",
                                "01210340",
                                "01210399"]
                        }
                    ]
                },
                {
                    text:"中介公告",
                    "code":"I17",
                    "pcode":"I17",
                    isexpand:false,
                    "children":[
                        {
                            text:"审计报告",
                            "code":"I1701",
                            "pcode":"I17",
                            "classfyId":["012913"]
                        },
                        {
                            text:"资产评估报告",
                            "code":"I1702",
                            "pcode":"I17",
                            "classfyId":["012905"]
                        },
                        {
                            text:"项目可行性报告",
                            "code":"I1703",
                            "pcode":"I17",
                            "classfyId":["012907"]
                        },
                        {
                            text:"独立财务顾问报告",
                            "code":"I1704",
                            "pcode":"I17",
                            "classfyId":["012901"]
                        },
                        {
                            text:"法律意见书",
                            "code":"I1705",
                            "pcode":"I17",
                            "classfyId":["012903"]
                        },
                        {
                            text:"投资价值分析报告",
                            "code":"I1706",
                            "pcode":"I17",
                            "classfyId":["012909"]
                        },
                        {
                            text:"其他中介报告",
                            "code":"I1707",
                            "pcode":"I17",
                            "classfyId":["0129",
                                "012911",
                                "012915",
                                "012917"]
                        }
                    ]
                },
                {
                    text:"股东大会",
                    "code":"I18",
                    "pcode":"I18",
                    isexpand:false,
                    "children":[
                        {
                            text:"股东大会通知",
                            "code":"I1801",
                            "pcode":"I18",
                            "classfyId":["011901",
                                "01190101",
                                "01190110",
                                "01190120",
                                "01190130"]
                        },
                        {
                            text:"股东大会决议",
                            "code":"I1802",
                            "pcode":"I18",
                            "classfyId":["011905",
                                "01190501",
                                "01190510"]
                        },
                        {
                            text:"股东大会变更",
                            "code":"I1803",
                            "pcode":"I18",
                            "classfyId":["011903",
                                "01190301",
                                "01190310",
                                "01190320",
                                "01190330",
                                "01190340",
                                "01190350"]
                        },
                        {
                            text:"董事会公告",
                            "code":"I1804",
                            "pcode":"I18",
                            "classfyId":["01010503",
                                "01239901"]
                        },
                        {
                            text:"监事会公告",
                            "code":"I1805",
                            "pcode":"I18",
                            "classfyId":["01010505",
                                "01239910"]
                        }
                    ]
                },
                {
                    text:"公司制度",
                    "code":"I19",
                    "pcode":"I19",
                    isexpand:false,
                    "children":[
                        {
                            text:"公司章程",
                            "code":"I1901",
                            "pcode":"I19",
                            "classfyId":["013101",
                                "013103",
                                "01310301",
                                "01310310"]
                        },
                        {
                            text:"议事规则",
                            "code":"I1902",
                            "pcode":"I19",
                            "classfyId":["01310501",
                                "01310505",
                                "01310510",
                                "01310515",
                                "01310520",
                                "01310525",
                                "01310530",
                                "01310535",
                                "01310540"]
                        },
                        {
                            text:"独董制度",
                            "code":"I1903",
                            "pcode":"I19",
                            "classfyId":["01310545",
                                "01310550",
                                "01310555",
                                "01310560",
                                "01310565"]
                        },
                        {
                            text:"信息披露制度",
                            "code":"I1904",
                            "pcode":"I19",
                            "classfyId":["01319901"]
                        },
                        {
                            text:"总经理工作细则",
                            "code":"I1905",
                            "pcode":"I19",
                            "classfyId":["01319910"]
                        },
                        {
                            text:"财务管理制度",
                            "code":"I1906",
                            "pcode":"I19",
                            "classfyId":["%财务管理%"]
                        },
                        {
                            text:"公司管理制度",
                            "code":"I1907",
                            "pcode":"I19",
                            "classfyId":["%公司管理%"]
                        }
                    ]
                }
            ]
        }
    ];