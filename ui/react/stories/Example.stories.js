import React, { Component } from 'react';
import WorkbookEditor from '../src/view/WorkbookEditor';
import Workbook from '../src/model/Workbook';
import { EditResponse } from '../src/action';
import '../theme/default/theme.css';

const fakeService = {
    call: (request, okCallback, errorCallback) => {
        okCallback(new EditResponse(request.txId, 0, 'Ok'));
    }
};

class ExampleStories extends Component {
    render() {
        return (
            <div>
                <h3>WorkbookEditor Example</h3>
                <div style={{
                    height: 500,
                    padding: 10,
                    background: '#000'
                }}>
                    <WorkbookEditor
                        service={fakeService}
                        workbook={workbookExample} />
                </div>
            </div>
        );
    }
}

export default ExampleStories;

const workbookExample = Workbook.deserialize({
    "name": "UBT系统监控看板",
    "sheets": [
        {
            "name": "Overview",
            "assets": [
                {
                    "chart": {
                        "name": "71e35be6-625f-4687-9b87-5e856314d90a",
                        "type": "Gauge",
                        "title": {
                            "string": "近10分钟Spaceport消费延迟"
                        },
                        "categories": {},
                        "series": [
                            {
                                "name": {
                                    "string": ""
                                },
                                "yValues": {
                                    "list": {
                                        "items": [
                                            {
                                                "decimal": "422.05675"
                                            }
                                        ]
                                    }
                                }
                            }
                        ],
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 1,
                                    "end": 4
                                },
                                "row": {
                                    "start": 1,
                                    "end": 7
                                }
                            }
                        },
                        "binder": {
                            "data": {
                                "formulaString": "Lag!spaceport!A:B"
                            },
                            "orientation": "ORIENT_HORIZONTAL",
                            "seriesNamePlacement": "PLCMT_LEFT"
                        },
                        "xAxis": {
                            "interval": {}
                        },
                        "yAxis": {
                            "title": "ms",
                            "interval": {
                                "to": 1800000
                            },
                            "bands": [
                                {
                                    "interval": {
                                        "to": 60000
                                    },
                                    "color": {
                                        "red": 0.49411765,
                                        "green": 0.827451,
                                        "blue": 0.12941177,
                                        "alpha": 1
                                    }
                                },
                                {
                                    "interval": {
                                        "from": 60000,
                                        "to": 300000
                                    },
                                    "color": {
                                        "red": 0.72156864,
                                        "green": 0.9137255,
                                        "blue": 0.5254902,
                                        "alpha": 1
                                    }
                                },
                                {
                                    "interval": {
                                        "from": 300000,
                                        "to": 900000
                                    },
                                    "color": {
                                        "red": 0.972549,
                                        "green": 0.90588236,
                                        "blue": 0.10980392,
                                        "alpha": 1
                                    }
                                },
                                {
                                    "interval": {
                                        "from": 900000,
                                        "to": 1800000
                                    },
                                    "color": {
                                        "red": 0.8156863,
                                        "green": 0.007843138,
                                        "blue": 0.105882354,
                                        "alpha": 1
                                    }
                                }
                            ]
                        },
                        "zAxis": {
                            "interval": {}
                        }
                    }
                },
                {
                    "chart": {
                        "name": "5451453a-8c41-4b5b-a7d0-668fdf7fd750",
                        "type": "Line",
                        "title": {
                            "string": "七天内各平台PV采集率"
                        },
                        "categories": {},
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 1,
                                    "end": 7
                                },
                                "row": {
                                    "start": 7,
                                    "end": 14
                                }
                            }
                        },
                        "binder": {
                            "data": {
                                "formulaString": "QA!\"pv-loss-by-platform\"!A:H"
                            },
                            "orientation": "ORIENT_HORIZONTAL",
                            "categoriesPlacement": "PLCMT_TOP",
                            "seriesNamePlacement": "PLCMT_LEFT"
                        },
                        "xAxis": {
                            "title": "日期",
                            "interval": {}
                        },
                        "yAxis": {
                            "title": "采集率",
                            "interval": {
                                "to": 1
                            }
                        },
                        "zAxis": {
                            "interval": {}
                        }
                    }
                },
                {
                    "chart": {
                        "name": "4111e918-a03c-43a1-8861-ec86d5891a83",
                        "type": "Pie",
                        "title": {
                            "string": "移动端数据上报延迟分布"
                        },
                        "categories": {
                            "list": {
                                "items": [
                                    {
                                        "string": "late=(0s,5s]"
                                    },
                                    {
                                        "string": "late=(5s,15s]"
                                    },
                                    {
                                        "string": "late=(15s,30s]"
                                    },
                                    {
                                        "string": "late=(1m,15m]"
                                    },
                                    {
                                        "string": "late=(30s,60s]"
                                    },
                                    {
                                        "string": "late=(1h,12h]"
                                    },
                                    {
                                        "string": "late=(15m,60m]"
                                    },
                                    {
                                        "string": "late=(12h,24h]"
                                    },
                                    {
                                        "string": "late=(24h,72h]"
                                    },
                                    {
                                        "string": "late=Other"
                                    }
                                ]
                            }
                        },
                        "series": [
                            {
                                "yValues": {
                                    "list": {
                                        "items": [
                                            {
                                                "decimal": "112249541"
                                            },
                                            {
                                                "decimal": "1.24099E+7"
                                            },
                                            {
                                                "decimal": "4490958.0"
                                            },
                                            {
                                                "decimal": "4210747.0"
                                            },
                                            {
                                                "decimal": "1910778.0"
                                            },
                                            {
                                                "decimal": "1833721.0"
                                            },
                                            {
                                                "decimal": "1389336.0"
                                            },
                                            {
                                                "decimal": "898948.0"
                                            },
                                            {
                                                "decimal": "729185.0"
                                            },
                                            {
                                                "decimal": "466058.0"
                                            }
                                        ]
                                    }
                                }
                            }
                        ],
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 7,
                                    "end": 13
                                },
                                "row": {
                                    "start": 1,
                                    "end": 7
                                }
                            }
                        },
                        "binder": {
                            "data": {
                                "formulaString": "Lag!\"mobile-sdk\"!A:B"
                            },
                            "orientation": "ORIENT_VERTICAL",
                            "categoriesPlacement": "PLCMT_LEFT"
                        },
                        "xAxis": {
                            "interval": {}
                        },
                        "yAxis": {
                            "interval": {}
                        },
                        "zAxis": {
                            "interval": {}
                        }
                    }
                },
                {
                    "chart": {
                        "name": "174845b5-30cf-4897-a8be-9233afe77b33",
                        "type": "Gauge",
                        "title": {
                            "string": "近1小时Custom数据量"
                        },
                        "categories": {
                            "list": {
                                "items": [
                                    {
                                        "string": ""
                                    }
                                ]
                            }
                        },
                        "series": [
                            {
                                "yValues": {
                                    "list": {
                                        "items": [
                                            {
                                                "decimal": "433694198"
                                            }
                                        ]
                                    }
                                }
                            }
                        ],
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 4,
                                    "end": 7
                                },
                                "row": {
                                    "start": 1,
                                    "end": 7
                                }
                            }
                        },
                        "binder": {
                            "data": {
                                "formulaString": "DT!\"custom-count\"!A:B"
                            },
                            "orientation": "ORIENT_VERTICAL",
                            "categoriesPlacement": "PLCMT_LEFT"
                        },
                        "xAxis": {
                            "interval": {}
                        },
                        "yAxis": {
                            "title": "条数",
                            "interval": {
                                "to": 1000000000
                            },
                            "bands": [
                                {
                                    "interval": {
                                        "to": 500000000
                                    },
                                    "color": {
                                        "red": 0.49411765,
                                        "green": 0.827451,
                                        "blue": 0.12941177,
                                        "alpha": 1
                                    }
                                },
                                {
                                    "interval": {
                                        "from": 500000000,
                                        "to": 750000000
                                    },
                                    "color": {
                                        "red": 0.972549,
                                        "green": 0.90588236,
                                        "blue": 0.10980392,
                                        "alpha": 1
                                    }
                                },
                                {
                                    "interval": {
                                        "from": 750000000,
                                        "to": 1000000000
                                    },
                                    "color": {
                                        "red": 0.8156863,
                                        "green": 0.007843138,
                                        "blue": 0.105882354,
                                        "alpha": 1
                                    }
                                }
                            ]
                        },
                        "zAxis": {
                            "interval": {}
                        }
                    }
                },
                {
                    "chart": {
                        "name": "38c594a3-2f18-4120-b017-d4c2ca4f6ae6",
                        "type": "Pie",
                        "title": {
                            "string": "SDK版本对比（基于近1小时PV量）"
                        },
                        "categories": {
                            "list": {
                                "items": [
                                    {
                                        "string": "mCollector=Mobile-SDK/4.0.0"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/2.6.9"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/3.1.0"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/4.0.0"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/2.6.8"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.7.3"
                                    },
                                    {
                                        "string": "mCollector=Mobile-SDK/1.6.19-realtime"
                                    },
                                    {
                                        "string": "mCollector=Mobile-SDK/1.7.7-realtime"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/2.6.3"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.7.6"
                                    },
                                    {
                                        "string": "mCollector=Mobile-SDK/3.0.0"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/3.0.1"
                                    },
                                    {
                                        "string": "mCollector=Mobile-SDK/4.0.0-realtime"
                                    },
                                    {
                                        "string": "mCollector=Mobile-SDK/1.6.17-realtime"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.6.5"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.6.16"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/Unknown"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/3.0.2"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/2.3.4"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.5.7"
                                    },
                                    {
                                        "string": "mCollector=Mobile-SDK/1.6.5-realtime"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.7.7"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.5.6"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.1.9"
                                    },
                                    {
                                        "string": "mCollector=Mobile-SDK/1.6.14-realtime"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.5.2"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/2.2.3"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.6.19"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.2.0"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.5.3"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/1.5.4"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/2.4.8"
                                    },
                                    {
                                        "string": "mCollector=Mobile-SDK/1.6.12-realtime"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/2.3.6"
                                    },
                                    {
                                        "string": "mCollector=JS-SDK/2.3.8"
                                    }
                                ]
                            }
                        },
                        "series": [
                            {
                                "yValues": {
                                    "list": {
                                        "items": [
                                            {
                                                "decimal": "2305565.0"
                                            },
                                            {
                                                "decimal": "1839130.0"
                                            },
                                            {
                                                "decimal": "1675326.0"
                                            },
                                            {
                                                "decimal": "1648246.0"
                                            },
                                            {
                                                "decimal": "162989.0"
                                            },
                                            {
                                                "decimal": "19463.0"
                                            },
                                            {
                                                "decimal": "12831.0"
                                            },
                                            {
                                                "decimal": "8751.0"
                                            },
                                            {
                                                "decimal": "5206.0"
                                            },
                                            {
                                                "decimal": "1989.0"
                                            },
                                            {
                                                "decimal": "968.0"
                                            },
                                            {
                                                "decimal": "617.0"
                                            },
                                            {
                                                "decimal": "538.0"
                                            },
                                            {
                                                "decimal": "494.0"
                                            },
                                            {
                                                "decimal": "433.0"
                                            },
                                            {
                                                "decimal": "201.0"
                                            },
                                            {
                                                "decimal": "201.0"
                                            },
                                            {
                                                "decimal": "131.0"
                                            },
                                            {
                                                "decimal": "122.0"
                                            },
                                            {
                                                "decimal": "96.0"
                                            },
                                            {
                                                "decimal": "71.0"
                                            },
                                            {
                                                "decimal": "32.0"
                                            },
                                            {
                                                "decimal": "27.0"
                                            },
                                            {
                                                "decimal": "23.0"
                                            },
                                            {
                                                "decimal": "19.0"
                                            },
                                            {
                                                "decimal": "12.0"
                                            },
                                            {
                                                "decimal": "8.0"
                                            },
                                            {
                                                "decimal": "7.0"
                                            },
                                            {
                                                "decimal": "5.0"
                                            },
                                            {
                                                "decimal": "4.0"
                                            },
                                            {
                                                "decimal": "4.0"
                                            },
                                            {
                                                "decimal": "1.0"
                                            },
                                            {
                                                "decimal": "1.0"
                                            },
                                            {
                                                "decimal": "1.0"
                                            },
                                            {
                                                "decimal": "1.0"
                                            }
                                        ]
                                    }
                                }
                            }
                        ],
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 7,
                                    "end": 13
                                },
                                "row": {
                                    "start": 7,
                                    "end": 14
                                }
                            }
                        },
                        "binder": {
                            "data": {
                                "formulaString": "DT!\"sdk-count\"!A:B"
                            },
                            "orientation": "ORIENT_VERTICAL",
                            "categoriesPlacement": "PLCMT_LEFT"
                        },
                        "xAxis": {
                            "interval": {}
                        },
                        "yAxis": {
                            "interval": {}
                        },
                        "zAxis": {
                            "interval": {}
                        }
                    }
                }
            ],
            "layout": {
                "display": "DISP_GRID",
                "width": 1200,
                "height": 720,
                "align": "PLCMT_LEFT",
                "verticalAlign": "PLCMT_TOP",
                "grid": {
                    "columns": 12
                }
            }
        },
        {
            "name": "Lag",
            "assets": [
                {
                    "table": {
                        "name": "spaceport",
                        "rows": [
                            {
                                "cells": [
                                    {
                                        "value": {
                                            "string": ""
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "422.05675"
                                        }
                                    }
                                ]
                            }
                        ],
                        "automaton": {
                            "queryAutomaton": {
                                "template": {
                                    "scheme": "dashboard",
                                    "builtinParams": [
                                        {
                                            "name": "filter.MetricName",
                                            "value": {
                                                "string": "producerToBoltDelay"
                                            }
                                        },
                                        {
                                            "name": "metric",
                                            "value": {
                                                "string": "ctrip.storm.metric"
                                            }
                                        },
                                        {
                                            "name": "aggregator",
                                            "value": {
                                                "string": "avg"
                                            }
                                        },
                                        {
                                            "name": "interval",
                                            "value": {
                                                "string": "5m"
                                            }
                                        },
                                        {
                                            "name": "endTime",
                                            "value": {
                                                "formulaString": "NOW()",
                                                "date": "2018-09-14T04:35:27.196Z"
                                            }
                                        },
                                        {
                                            "name": "filter.Topology",
                                            "value": {
                                                "string": "user_behavior_cmatrix_pageview"
                                            }
                                        },
                                        {
                                            "name": "points",
                                            "value": {
                                                "decimal": "1"
                                            }
                                        }
                                    ]
                                },
                                "query": {
                                    "scheme": "dashboard",
                                    "params": [
                                        {
                                            "name": "filter.MetricName",
                                            "value": {
                                                "string": "producerToBoltDelay"
                                            }
                                        },
                                        {
                                            "name": "metric",
                                            "value": {
                                                "string": "ctrip.storm.metric"
                                            }
                                        },
                                        {
                                            "name": "aggregator",
                                            "value": {
                                                "string": "avg"
                                            }
                                        },
                                        {
                                            "name": "interval",
                                            "value": {
                                                "string": "5m"
                                            }
                                        },
                                        {
                                            "name": "endTime",
                                            "value": {
                                                "date": "2018-09-14T04:35:27.196Z"
                                            }
                                        },
                                        {
                                            "name": "filter.Topology",
                                            "value": {
                                                "string": "user_behavior_cmatrix_pageview"
                                            }
                                        },
                                        {
                                            "name": "points",
                                            "value": {
                                                "decimal": "1"
                                            }
                                        }
                                    ]
                                }
                            }
                        },
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 1,
                                    "end": 7
                                },
                                "row": {
                                    "start": 1,
                                    "end": 10
                                }
                            }
                        }
                    }
                },
                {
                    "table": {
                        "name": "mobile-sdk",
                        "rows": [
                            {
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(0s,5s]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "112250887"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 1,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(5s,15s]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "12410305"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 2,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(15s,30s]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "4491057.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 3,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(1m,15m]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "4210862.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 4,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(30s,60s]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1910905.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 5,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(1h,12h]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1833866.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 6,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(15m,60m]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1389402.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 7,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(12h,24h]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "899160.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 8,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=(24h,72h]"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "729212.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 9,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "late=Other"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "466070.0"
                                        }
                                    }
                                ]
                            }
                        ],
                        "automaton": {
                            "queryAutomaton": {
                                "template": {
                                    "scheme": "dashboard",
                                    "builtinParams": [
                                        {
                                            "name": "metric",
                                            "value": {
                                                "string": "fx.ubt.data.late"
                                            }
                                        },
                                        {
                                            "name": "aggregator",
                                            "value": {
                                                "string": "count"
                                            }
                                        },
                                        {
                                            "name": "interval",
                                            "value": {
                                                "string": "10m"
                                            }
                                        },
                                        {
                                            "name": "endTime",
                                            "value": {
                                                "formulaString": "NOW()",
                                                "date": "2018-09-14T04:35:27.196Z"
                                            }
                                        },
                                        {
                                            "name": "groupBy",
                                            "value": {
                                                "string": "late"
                                            }
                                        },
                                        {
                                            "name": "points",
                                            "value": {
                                                "decimal": "1"
                                            }
                                        }
                                    ]
                                },
                                "query": {
                                    "scheme": "dashboard",
                                    "params": [
                                        {
                                            "name": "metric",
                                            "value": {
                                                "string": "fx.ubt.data.late"
                                            }
                                        },
                                        {
                                            "name": "aggregator",
                                            "value": {
                                                "string": "count"
                                            }
                                        },
                                        {
                                            "name": "interval",
                                            "value": {
                                                "string": "10m"
                                            }
                                        },
                                        {
                                            "name": "endTime",
                                            "value": {
                                                "date": "2018-09-14T04:35:27.196Z"
                                            }
                                        },
                                        {
                                            "name": "groupBy",
                                            "value": {
                                                "string": "late"
                                            }
                                        },
                                        {
                                            "name": "points",
                                            "value": {
                                                "decimal": "1"
                                            }
                                        }
                                    ]
                                }
                            }
                        },
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 7,
                                    "end": 13
                                },
                                "row": {
                                    "start": 1,
                                    "end": 10
                                }
                            }
                        }
                    }
                }
            ],
            "layout": {
                "display": "DISP_GRID",
                "width": 1200,
                "height": 720,
                "align": "PLCMT_LEFT",
                "verticalAlign": "PLCMT_TOP",
                "grid": {
                    "columns": 12
                }
            }
        },
        {
            "name": "DT",
            "assets": [
                {
                    "table": {
                        "name": "custom-count",
                        "rows": [
                            {
                                "cells": [
                                    {
                                        "value": {
                                            "string": ""
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "435348258"
                                        }
                                    }
                                ]
                            }
                        ],
                        "automaton": {
                            "queryAutomaton": {
                                "template": {
                                    "scheme": "dashboard",
                                    "builtinParams": [
                                        {
                                            "name": "metric",
                                            "value": {
                                                "string": "fx.ubt.custom.count"
                                            }
                                        },
                                        {
                                            "name": "aggregator",
                                            "value": {
                                                "string": "sum"
                                            }
                                        },
                                        {
                                            "name": "interval",
                                            "value": {
                                                "string": "1h"
                                            }
                                        },
                                        {
                                            "name": "endTime",
                                            "value": {
                                                "formulaString": "NOW()",
                                                "date": "2018-09-14T04:35:27.196Z"
                                            }
                                        },
                                        {
                                            "name": "points",
                                            "value": {
                                                "decimal": "1"
                                            }
                                        }
                                    ]
                                },
                                "query": {
                                    "scheme": "dashboard",
                                    "params": [
                                        {
                                            "name": "metric",
                                            "value": {
                                                "string": "fx.ubt.custom.count"
                                            }
                                        },
                                        {
                                            "name": "aggregator",
                                            "value": {
                                                "string": "sum"
                                            }
                                        },
                                        {
                                            "name": "interval",
                                            "value": {
                                                "string": "1h"
                                            }
                                        },
                                        {
                                            "name": "endTime",
                                            "value": {
                                                "date": "2018-09-14T04:35:27.196Z"
                                            }
                                        },
                                        {
                                            "name": "points",
                                            "value": {
                                                "decimal": "1"
                                            }
                                        }
                                    ]
                                }
                            }
                        },
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 1,
                                    "end": 7
                                },
                                "row": {
                                    "start": 1,
                                    "end": 6
                                }
                            }
                        }
                    }
                },
                {
                    "table": {
                        "name": "sdk-count",
                        "rows": [
                            {
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/4.0.0"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "2322041.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 1,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/2.6.9"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1851007.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 2,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/3.1.0"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1693296.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 3,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/4.0.0"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1663580.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 4,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/2.6.8"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "164003.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 5,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.7.3"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "19463.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 6,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/1.6.19-realtime"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "12881.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 7,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/1.7.7-realtime"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "8781.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 8,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/2.6.3"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "5206.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 9,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.7.6"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "2000.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 10,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/3.0.0"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "970.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 11,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/3.0.1"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "621.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 12,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/4.0.0-realtime"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "539.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 13,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/1.6.17-realtime"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "497.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 14,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.6.5"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "434.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 15,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.6.16"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "201.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 16,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/Unknown"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "201.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 17,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/3.0.2"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "131.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 18,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/2.3.4"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "122.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 19,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.5.7"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "96.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 20,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/1.6.5-realtime"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "71.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 21,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.7.7"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "34.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 22,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.5.6"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "27.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 23,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.1.9"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "23.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 24,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/1.6.14-realtime"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "19.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 25,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.5.2"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "12.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 26,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/2.2.3"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "8.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 27,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.6.19"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "7.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 28,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.2.0"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "5.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 29,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.5.3"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "4.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 30,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/1.5.4"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "4.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 31,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/2.4.8"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 32,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=Mobile-SDK/1.6.12-realtime"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 33,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/2.3.6"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1.0"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 34,
                                "cells": [
                                    {
                                        "value": {
                                            "string": "mCollector=JS-SDK/2.3.8"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "decimal": "1.0"
                                        }
                                    }
                                ]
                            }
                        ],
                        "automaton": {
                            "queryAutomaton": {
                                "template": {
                                    "scheme": "dashboard",
                                    "builtinParams": [
                                        {
                                            "name": "metric",
                                            "value": {
                                                "string": "fx.ubt.collector.version"
                                            }
                                        },
                                        {
                                            "name": "aggregator",
                                            "value": {
                                                "string": "sum"
                                            }
                                        },
                                        {
                                            "name": "interval",
                                            "value": {
                                                "string": "1h"
                                            }
                                        },
                                        {
                                            "name": "endTime",
                                            "value": {
                                                "formulaString": "NOW()",
                                                "date": "2018-09-14T04:35:27.196Z"
                                            }
                                        },
                                        {
                                            "name": "groupBy",
                                            "value": {
                                                "string": "mCollector"
                                            }
                                        },
                                        {
                                            "name": "points",
                                            "value": {
                                                "decimal": "1"
                                            }
                                        }
                                    ]
                                },
                                "query": {
                                    "scheme": "dashboard",
                                    "params": [
                                        {
                                            "name": "metric",
                                            "value": {
                                                "string": "fx.ubt.collector.version"
                                            }
                                        },
                                        {
                                            "name": "aggregator",
                                            "value": {
                                                "string": "sum"
                                            }
                                        },
                                        {
                                            "name": "interval",
                                            "value": {
                                                "string": "1h"
                                            }
                                        },
                                        {
                                            "name": "endTime",
                                            "value": {
                                                "date": "2018-09-14T04:35:27.196Z"
                                            }
                                        },
                                        {
                                            "name": "groupBy",
                                            "value": {
                                                "string": "mCollector"
                                            }
                                        },
                                        {
                                            "name": "points",
                                            "value": {
                                                "decimal": "1"
                                            }
                                        }
                                    ]
                                }
                            }
                        },
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 7,
                                    "end": 13
                                },
                                "row": {
                                    "start": 1,
                                    "end": 13
                                }
                            }
                        }
                    }
                }
            ],
            "layout": {
                "display": "DISP_GRID",
                "width": 1200,
                "height": 720,
                "align": "PLCMT_LEFT",
                "verticalAlign": "PLCMT_TOP",
                "grid": {
                    "columns": 12
                }
            }
        },
        {
            "name": "QA",
            "assets": [
                {
                    "table": {
                        "name": "pv-loss",
                        "rows": [
                            {
                                "cells": [
                                    {
                                        "value": {
                                            "string": "date"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "platform"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "string": "ratio"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 1,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-12T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "Android"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.1003"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 2,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-12T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "iOS"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.9844"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 3,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-11T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "Android"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.1052"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 4,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-11T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "iOS"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.9844"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 5,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-10T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "Android"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.1070"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 6,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-10T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "iOS"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.9642"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 7,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-09T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "Android"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.1110"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 8,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-09T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "iOS"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.9637"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 9,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-08T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "Android"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.1066"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 10,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-08T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "iOS"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.9603"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 11,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-07T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "Android"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.1094"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 12,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-07T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "iOS"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.9835"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 13,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-06T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "Android"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.1167"
                                        }
                                    }
                                ]
                            },
                            {
                                "rowIndex": 14,
                                "cells": [
                                    {
                                        "value": {
                                            "date": "2018-09-06T16:00:00Z"
                                        }
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {
                                            "string": "iOS"
                                        }
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {
                                            "decimal": "0.9842"
                                        }
                                    }
                                ]
                            }
                        ],
                        "automaton": {
                            "queryAutomaton": {
                                "template": {
                                    "scheme": "mysql://default",
                                    "builtinParams": [
                                        {
                                            "name": "startDate",
                                            "value": {
                                                "formulaString": "TODAY()-7",
                                                "date": "2018-09-06T16:00:00Z"
                                            }
                                        },
                                        {
                                            "name": "sql",
                                            "value": {
                                                "string": "select `sdate` `date`, `platform`, sum(`actual`)/sum(`estimate`) `ratio` from stat_mob_seq_accuracy where sdate >= #{startDate} and `actual` >= 1000000 group by `sdate`, `platform` order by `sdate` desc limit 10000"
                                            }
                                        }
                                    ]
                                },
                                "query": {
                                    "scheme": "mysql://default",
                                    "params": [
                                        {
                                            "name": "startDate",
                                            "value": {
                                                "date": "2018-09-06T16:00:00Z"
                                            }
                                        },
                                        {
                                            "name": "sql",
                                            "value": {
                                                "string": "select `sdate` `date`, `platform`, sum(`actual`)/sum(`estimate`) `ratio` from stat_mob_seq_accuracy where sdate >= #{startDate} and `actual` >= 1000000 group by `sdate`, `platform` order by `sdate` desc limit 10000"
                                            }
                                        }
                                    ]
                                }
                            }
                        },
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 1,
                                    "end": 7
                                },
                                "row": {
                                    "start": 1,
                                    "end": 14
                                }
                            }
                        }
                    }
                },
                {
                    "table": {
                        "name": "pv-loss-by-platform",
                        "rows": [
                            {
                                "cells": [
                                    {
                                        "value": {}
                                    },
                                    {
                                        "columnIndex": 1,
                                        "value": {}
                                    },
                                    {
                                        "columnIndex": 2,
                                        "value": {}
                                    },
                                    {
                                        "columnIndex": 3,
                                        "value": {}
                                    },
                                    {
                                        "columnIndex": 4,
                                        "value": {}
                                    },
                                    {
                                        "columnIndex": 5,
                                        "value": {}
                                    },
                                    {
                                        "columnIndex": 6,
                                        "value": {}
                                    },
                                    {
                                        "columnIndex": 7,
                                        "value": {}
                                    }
                                ]
                            }
                        ],
                        "automaton": {
                            "pivotAutomaton": {
                                "data": {
                                    "formulaString": "\"pv-loss\"!A:C"
                                },
                                "rows": [
                                    {
                                        "field": "platform"
                                    }
                                ],
                                "columns": [
                                    {
                                        "field": "date"
                                    }
                                ],
                                "values": [
                                    {
                                        "field": "ratio",
                                        "aggregateType": "AT_SUMMARY"
                                    }
                                ]
                            }
                        },
                        "layout": {
                            "display": "DISP_BLOCK",
                            "align": "PLCMT_LEFT",
                            "verticalAlign": "PLCMT_TOP",
                            "grid": {
                                "column": {
                                    "start": 7,
                                    "end": 13
                                },
                                "row": {
                                    "start": 1,
                                    "end": 14
                                }
                            }
                        }
                    }
                }
            ],
            "layout": {
                "display": "DISP_GRID",
                "width": 1200,
                "height": 720,
                "align": "PLCMT_LEFT",
                "verticalAlign": "PLCMT_TOP",
                "grid": {
                    "columns": 12
                }
            }
        }
    ]
});
