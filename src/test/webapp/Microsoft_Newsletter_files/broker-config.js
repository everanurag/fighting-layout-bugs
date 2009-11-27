/*
Copyright (c) 2009, comScore Inc. All rights reserved.
version: 4.5.3
*/

COMSCORE.SiteRecruit.Broker.config = {
	version: "4.5.3",
		testMode: false,
	// cookie settings
	cookie:{
		name: 'msresearch',
		path: '/',
		domain:  '.microsoft.com' ,
		duration: 90,
		rapidDuration: 0,
		expireDate: ''
	},

	eddListenerUrl: '',
	
	// optional prefix for pagemapping's pageconfig file
	prefixUrl: "",

		mapping:[
	// m=regex match, c=page config file (prefixed with configUrl), f=frequency
	{m: '//[\\w\\.-]+/athome/', c: 'inv_c_3331mt3.js', f: 0.01, p: 0 	}
	,{m: '//[\\w\\.-]+/atwork', c: 'inv_c_3331mt5.js', f: 0.043, p: 0 	}
	,{m: '//[\\w\\.-]+/australia/athome/', c: 'inv_c_p15466742-au-372.js', f: 0.45, p: 0 	}
	,{m: '//[\\w\\.-]+/australia/athome/default\\.mspx$', c: 'inv_c_p15466742-au-372-flashfix.js', f: 0.25, p: 1 	}
	,{m: '//[\\w\\.-]+/australia/business/', c: 'inv_c_p15466742-au-373.js', f: 0.5, p: 0 	}
	,{m: '//[\\w\\.-]+/australia/business/(default\\.aspx)?$', c: 'inv_c_p15466742-au-373-SB-FIXED.js', f: 0.3, p: 1 	}
	,{m: '//[\\w\\.-]+/australia/smallbusiness/', c: 'inv_c_p15466742-au-6.js', f: 0.18, p: 0 	}
	,{m: '//[\\w\\.-]+/australia/windows/', c: 'inv_c_p15466742-au-826.js', f: 0.038, p: 0 	}
	,{m: '//[\\w\\.-]+/canada/windows/', c: 'inv_c_p37131508-Canada.js', f: 0.28, p: 0 	}
	,{m: '//[\\w\\.-]+/de/de/(default\\.aspx)?$', c: 'inv_c_p15466742-p17637473-Germany-HP.js', f: 0.011, p: 0 	}
	,{m: '//[\\w\\.-]+/downloads/(en/|.*?displaylang=en)', c: 'inv_c_3331mt13_NEW_751-753.js', f: 0.00037, p: 0 	}
	,{m: '//[\\w\\.-]+/dynamics(/(?!dynamicsresearch.mspx|everyonegetsit)|$)', c: 'inv_c_3331mt14_NEW-750.js', f: 0.19, p: 0 	}
	,{m: '//[\\w\\.-]+/dynamics/asmartmove/default\\.mspx', c: 'inv_c_3331mt14-SL-fix_NEW-750.js', f: 0.1836, p: 3 	}
	,{m: '//[\\w\\.-]+/dynamics/default\\.mspx$', c: 'inv_c_3331mt14_flashfix_NEW-750.js', f: 0.1836, p: 1 	}
	,{m: '//[\\w\\.-]+/en/au/', c: 'inv_c_p15466742-AU-HP-369.js', f: 0.045, p: 0 	}
	,{m: '//[\\w\\.-]+/en/us/default\\.aspx', c: 'inv_c_p15394611-US-HP.js', f: 0.0066, p: 0 	}
	,{m: '//[\\w\\.-]+/fr/fr/(default\\.aspx)?$', c: 'inv_c_p15466742-France-HP.js', f: 0.015, p: 0 	}
	,{m: '//[\\w\\.-]+/france/carrieres/', c: 'inv_c_p37116158-FR.js', f: 1, p: 0 	}
	,{m: '//[\\w\\.-]+/france/windows/', c: 'inv_c_p15466742_21.js', f: 0.0085, p: 0 	}
	,{m: '//[\\w\\.-]+/germany/branchen/', c: 'inv_c_DE-p15466742-Branchen.js', f: 0.5, p: 0 	}
	,{m: '//[\\w\\.-]+/germany/server(/|$)', c: 'inv_c_DE-wss-p12038685.js', f: 0.25, p: 0 	}
	,{m: '//[\\w\\.-]+/germany/windows(/|$)', c: 'inv_c_DE-windows-p12038685.js', f: 0.0085, p: 0 	}
	,{m: '//[\\w\\.-]+/italy/info/career/', c: 'inv_c_p37116158-IT.js', f: 1, p: 0 	}
	,{m: '//[\\w\\.-]+/ja/jp/', c: 'inv_c_p15466742-Japan-HP.js', f: 0.02, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/athome/', c: 'inv_c_JA-p15466742-athome.js', f: 0.0008, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/atwork/', c: 'inv_c_JA-p15466742-atwork.js', f: 0.0023, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/business/', c: 'inv_c_JA-p15466742-business.js', f: 0.04, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/careers/', c: 'inv_c_p37116158-JA.js', f: 1, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/servers/', c: 'inv_c_JA-p15466742-servers.js', f: 0.15, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/technet/', c: 'inv_c_JA-p12038685-technet.js', f: 0.002, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/users', c: 'inv_c_JA-p15466742-users.js', f: 0.003, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/users/default\\.mspx$', c: 'inv_c_JA-p15466742-users-Flashfix.js', f: 0.003, p: 1 	}
	,{m: '//[\\w\\.-]+/japan/windows(/(?!(downloads/ie/au\\.mspx)|(downloads/ie/iedelete\\.mspx))|$)', c: 'inv_c_JA-p15466742-windows.js', f: 0.0036, p: 0 	}
	,{m: '//[\\w\\.-]+/japan/windows/(digitallife|possibilities)/', c: 'inv_c_JA-p15466742-windows-digitallife.js', f: 0.0036, p: 1 	}
	,{m: '//[\\w\\.-]+/learning/en/us/(default\\.aspx)?$', c: 'inv_c_3331mt42.js', f: 0.5, p: 0 	}
	,{m: '//[\\w\\.-]+/licensing(/(?!(servicecenter)|(licensewise/product\\.aspx)|(licensewise/program\\.aspx)|(mla/select\\.aspx)))', c: 'inv_c_3331mt43.js', f: 0.1065, p: 0 	}
	,{m: 's://[\\w\\.-]+/licensing/servicecenter', c: 'inv_c_p40652279-VLSC.js', f: 0.025, p: 1 	}
	,{m: '//[\\w\\.-]+/licensing/servicecenter/', c: 'inv_c_p40652279-VLSC-France.js', f: 0.025, p: 2 		
		,prereqs:{
			content: [
				]
			,cookie: [
				]
			 ,language: 'fr' 			}
	}
	,{m: '//[\\w\\.-]+/licensing/servicecenter/', c: 'inv_c_p40652279-VLSC-Germany.js', f: 0.025, p: 2 		
		,prereqs:{
			content: [
				]
			,cookie: [
				]
			 ,language: 'de' 			}
	}
	,{m: '//[\\w\\.-]+/licensing/servicecenter/', c: 'inv_c_p40652279-VLSC-Japan.js', f: 0.025, p: 2 		
		,prereqs:{
			content: [
				]
			,cookie: [
				]
			 ,language: 'ja' 			}
	}
	,{m: '//[\\w\\.-]+/office/2007-rlt/en-us', c: 'inv_c_p40119999.js', f: 0.25, p: 0 	}
	,{m: '//[\\w\\.-]+/protect(/(?!computer/updates/bulletins)|$)', c: 'inv_c_3331mt4.js', f: 0.02, p: 0 	}
	,{m: '//[\\w\\.-]+/security', c: 'inv_c_3331mt49.js', f: 0.0059, p: 0 	}
	,{m: '//[\\w\\.-]+/sql/experience/(Default\\.aspx\\?loc=en)|(html/Default\\.aspx\\?loc=en)|(html/Events\\.aspx\\?loc=en)|(LearnSQL\\.aspx\\?h=t&loc=en)|(LearnSQL\\.aspx\\?loc=en)|(Events\\.aspx\\?loc=en)|(.*\\.wmv)', c: 'inv_c_blank.js', f: 0, p: 2 	}
	,{m: '//[\\w\\.-]+/(sql|sqlserver)', c: 'inv_c_3331mt52-p37985286-SQL.js', f: 0.049, p: 0 	}
	,{m: '//[\\w\\.-]+/sqlserver/2005/', c: 'inv_c_3331mt52-p37985286-SQL.js', f: 0.049, p: 1 	}
	,{m: '//[\\w\\.-]+/student/', c: 'inv_c_p40683318.js', f: 0.5, p: 0 	}
	,{m: '//[\\w\\.-]+/technet/(?!mnp_utility\\.mspx/(framesmenu|quicksearch|masthead)\\?url)', c: 'inv_c_p15808382-p26386365.js', f: 0.0025, p: 0 	}
	,{m: '//[\\w\\.-]+/technet/scriptcenter/', c: 'inv_c_p15808382-p26386365-TIER3.js', f: 0.0025, p: 1 	}
	,{m: '//[\\w\\.-]+/technet/security/', c: 'inv_c_p15808382-p26386365-TIER2.js', f: 0.0025, p: 1 	}
	,{m: '//[\\w\\.-]+/technet/(.*/subscriptions|support|community)/', c: 'inv_c_p15808382mt-technet.js', f: 0.0025, p: 1 	}
	,{m: '//[\\w\\.-]+/uk/windows/', c: 'inv_c_p37131508-UK.js', f: 0.035, p: 0 	}
	,{m: '//[\\w\\.-]+/video/', c: 'inv_c_p23275586.js', f: 0.5, p: 0 	}
	,{m: '//(sr-www|wwwstaging|www\\.microsoft)[\\w\\.-]+/windows/(?!enterprise)', c: 'inv_c_p25328149.js', f: 0.0012, p: 0 	}
	,{m: '//[\\w\\.-]+/windows/buy/', c: 'inv_c_p25328149-Buy-WLS-p38104477-BUY.js', f: 0.045, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/buy/windows-laptop-scout\\.aspx$', c: 'inv_c_p25328149-Buy-WLS-p38104477.js', f: 0.2, p: 2 	}
	,{m: '//[\\w\\.-]+/windows/(default\\.aspx)?$', c: 'inv_c_p25328149-HP_882.js', f: 0.013, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/downloads/', c: 'inv_c_p25328149-downloads-p34934647.js', f: 0.00677, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/downloads/ie/getitnow\\.mspx', c: 'inv_c_3331mt62-p25328149.js', f: 0.0012, p: 2 	}
	,{m: '//[\\w\\.-]+/windows/enterprise/(?!(default\\.(aspx|html|mspx))|$)', c: 'inv_c_p38361073-qInvite.js', f: 0.013, p: 0 	}
	,{m: '//[\\w\\.-]+/windows/internet-explorer/(?!welcome\\.aspx)', c: 'inv_c_3331mt62-p25328149.js', f: 0.0012, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/internet-explorer/videos\\.aspx$', c: 'inv_c_3331mt62-p25328149_SL-FIX.js', f: 0.0015, p: 2 	}
	,{m: '//[\\w\\.-]+/windows/possibilities/', c: 'inv_c_p25328149_SL-FIX.js', f: 0.0012, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/products/winfamily/ie(/|$)', c: 'inv_c_3331mt62-p25328149.js', f: 0.0012, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/windows-7/', c: 'inv_c_p34934887-p25328149.js', f: 0.0051, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/windows-laptop-scout/(default\\.aspx)?$', c: 'inv_c_p25328149_laptop-scout_SL-FIX.js', f: 0.2, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/windows-vista(/|$)', c: 'inv_c_3331mt64-p25328149.js', f: 0.011, p: 1 	}
	,{m: '//[\\w\\.-]+/windows/windows-vista/discover/', c: 'inv_c_3331mt64-p25328149_SL-FX.js', f: 0.013, p: 2 	}
	,{m: '//[\\w\\.-]+/windowsembedded/en-us/', c: 'inv_c_3331mt174.js', f: 0.5, p: 1 	}
	,{m: '//[\\w\\.-]+/windowsmobile', c: 'inv_c_3331mt173.js', f: 0.0012, p: 0 	}
	,{m: '//[\\w\\.-]+/windowsmobile/en-us/totalaccess/', c: 'inv_c_p30393194_3331mt173.js', f: 0.0012, p: 1 	}
]
};
COMSCORE.SiteRecruit.Broker.run();