(function($){"use strict";$(document).ready(function(){var getCalculation=function(diff){if(diff<60){return diff+" seconds ago";}
if(diff<3600){return Math.ceil(diff/60)+" minutes ago";}
if(diff<3600*24){return Math.ceil(diff/3600)+" hours ago";}
return null;};$('time[datetime]').each(function(){var $this=$(this),dtString=$this.attr("datetime"),dt=new Date(Date.parse(dtString)),now=new Date(),difference=Math.floor((now.getTime()-dt.getTime())/1000),format=$this.data("format")||'MMM D, YYYY';if($this.hasClass("oAutoRelative")&&(difference<3600*24*2)){return;}
$this.text(moment(dt).format(format,dt));});});}(jQuery));(function(Date,undefined){var origParse=Date.parse,numericKeys=[1,4,5,6,7,10,11];Date.parse=function(date){var timestamp,struct,minutesOffset=0;if((struct=/^(\d{4}|[+\-]\d{6})(?:-(\d{2})(?:-(\d{2}))?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(?:\.(\d{3}))?)?(?:(Z)|([+\-])(\d{2})(?::(\d{2}))?)?)?$/.exec(date))){for(var i=0,k;(k=numericKeys[i]);++i){struct[k]=+struct[k]||0;}
struct[2]=(+struct[2]||1)-1;struct[3]=+struct[3]||1;if(struct[8]!=='Z'&&struct[9]!==undefined){minutesOffset=struct[10]*60+struct[11];if(struct[9]==='+'){minutesOffset=0-minutesOffset;}}
timestamp=Date.UTC(struct[1],struct[2],struct[3],struct[4],struct[5]+minutesOffset,struct[6],struct[7]);}
else{timestamp=origParse?origParse(date):NaN;}
return timestamp;};}(Date));(function(Date,undefined){var moment,VERSION="1.6.1",round=Math.round,i,languages={},currentLanguage='en',hasModule=(typeof module!=='undefined'),langConfigProperties='months|monthsShort|monthsParse|weekdays|weekdaysShort|longDateFormat|calendar|relativeTime|ordinal|meridiem'.split('|'),aspNetJsonRegex=/^\/?Date\((\-?\d+)/i,formattingTokens=/(\[[^\[]*\])|(\\)?(Mo|MM?M?M?|Do|DDDo|DD?D?D?|dddd?|do?|w[o|w]?|YYYY|YY|a|A|hh?|HH?|mm?|ss?|SS?S?|zz?|ZZ?|LT|LL?L?L?)/g,parseMultipleFormatChunker=/([0-9a-zA-Z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+)/gi,parseTokenOneDigit=/\d/,parseTokenOneOrTwoDigits=/\d\d?/,parseTokenOneToThreeDigits=/\d{1,3}/,parseTokenTwoDigits=/\d\d/,parseTokenThreeDigits=/\d{3}/,parseTokenFourDigits=/\d{4}/,parseTokenWord=/[0-9a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+/i,parseTokenTimezone=/[\+\-]\d\d:?\d\d/i,parseTokenT=/T/i,isoRegex=/^\s*\d{4}-\d\d-\d\d(T(\d\d(:\d\d(:\d\d)?)?)?([\+\-]\d\d:?\d\d)?)?/,isoFormat='YYYY-MM-DDTHH:mm:ssZ',isoTimes=[['HH:mm:ss',/T\d\d:\d\d:\d\d/],['HH:mm',/T\d\d:\d\d/],['HH',/T\d\d/]],parseTimezoneChunker=/([\+\-]|\d\d)/gi,proxyGettersAndSetters='Month|Date|Hours|Minutes|Seconds|Milliseconds'.split('|'),durationGetters='years|months|days|hours|minutes|seconds|milliseconds'.split('|'),unitMillisecondFactors={'Milliseconds':1,'Seconds':1e3,'Minutes':6e4,'Hours':36e5,'Days':864e5,'Weeks':6048e5,'Months':2592e6,'Years':31536e6};function Moment(date,isUTC){this._d=date;this._isUTC=!!isUTC;}
function absRound(number){if(number<0){return Math.ceil(number);}else{return Math.floor(number);}}
function Duration(duration){var data=this._data={},years=duration.years||duration.y||0,months=duration.months||duration.M||0,weeks=duration.weeks||duration.w||0,days=duration.days||duration.d||0,hours=duration.hours||duration.h||0,minutes=duration.minutes||duration.m||0,seconds=duration.seconds||duration.s||0,milliseconds=duration.milliseconds||duration.ms||0;this._milliseconds=milliseconds+
seconds*1e3+
minutes*6e4+
hours*36e5;this._days=days+
weeks*7;this._months=months+
years*12;data.milliseconds=milliseconds%1000;seconds+=absRound(milliseconds/1000);data.seconds=seconds%60;minutes+=absRound(seconds/60);data.minutes=minutes%60;hours+=absRound(minutes/60);data.hours=hours%24;days+=absRound(hours/24);days+=weeks*7;data.days=days%30;months+=absRound(days/30);data.months=months%12;years+=absRound(months/12);data.years=years;}
function leftZeroFill(number,targetLength){var output=number+'';while(output.length<targetLength){output='0'+output;}
return output;}
function addOrSubtractDurationFromMoment(mom,duration,isAdding){var ms=duration._milliseconds,d=duration._days,M=duration._months,currentDate;if(ms){mom._d.setTime(+mom+ms*isAdding);}
if(d){mom.date(mom.date()+d*isAdding);}
if(M){currentDate=mom.date();mom.date(1).month(mom.month()+M*isAdding).date(Math.min(currentDate,mom.daysInMonth()));}}
function isArray(input){return Object.prototype.toString.call(input)==='[object Array]';}
function dateFromArray(input){return new Date(input[0],input[1]||0,input[2]||1,input[3]||0,input[4]||0,input[5]||0,input[6]||0);}
function formatMoment(m,inputString){var currentMonth=m.month(),currentDate=m.date(),currentYear=m.year(),currentDay=m.day(),currentHours=m.hours(),currentMinutes=m.minutes(),currentSeconds=m.seconds(),currentMilliseconds=m.milliseconds(),currentZone=-m.zone(),ordinal=moment.ordinal,meridiem=moment.meridiem;function replaceFunction(input){var a,b;switch(input){case'M':return currentMonth+1;case'Mo':return(currentMonth+1)+ordinal(currentMonth+1);case'MM':return leftZeroFill(currentMonth+1,2);case'MMM':return moment.monthsShort[currentMonth];case'MMMM':return moment.months[currentMonth];case'D':return currentDate;case'Do':return currentDate+ordinal(currentDate);case'DD':return leftZeroFill(currentDate,2);case'DDD':a=new Date(currentYear,currentMonth,currentDate);b=new Date(currentYear,0,1);return~~(((a-b)/864e5)+1.5);case'DDDo':a=replaceFunction('DDD');return a+ordinal(a);case'DDDD':return leftZeroFill(replaceFunction('DDD'),3);case'd':return currentDay;case'do':return currentDay+ordinal(currentDay);case'ddd':return moment.weekdaysShort[currentDay];case'dddd':return moment.weekdays[currentDay];case'w':a=new Date(currentYear,currentMonth,currentDate-currentDay+5);b=new Date(a.getFullYear(),0,4);return~~((a-b)/864e5/7+1.5);case'wo':a=replaceFunction('w');return a+ordinal(a);case'ww':return leftZeroFill(replaceFunction('w'),2);case'YY':return leftZeroFill(currentYear%100,2);case'YYYY':return currentYear;case'a':return meridiem?meridiem(currentHours,currentMinutes,false):(currentHours>11?'pm':'am');case'A':return meridiem?meridiem(currentHours,currentMinutes,true):(currentHours>11?'PM':'AM');case'H':return currentHours;case'HH':return leftZeroFill(currentHours,2);case'h':return currentHours%12||12;case'hh':return leftZeroFill(currentHours%12||12,2);case'm':return currentMinutes;case'mm':return leftZeroFill(currentMinutes,2);case's':return currentSeconds;case'ss':return leftZeroFill(currentSeconds,2);case'S':return~~(currentMilliseconds/100);case'SS':return leftZeroFill(~~(currentMilliseconds/10),2);case'SSS':return leftZeroFill(currentMilliseconds,3);case'Z':return(currentZone<0?'-':'+')+leftZeroFill(~~(Math.abs(currentZone)/60),2)+':'+leftZeroFill(~~(Math.abs(currentZone)%60),2);case'ZZ':return(currentZone<0?'-':'+')+leftZeroFill(~~(10*Math.abs(currentZone)/6),4);case'L':case'LL':case'LLL':case'LLLL':case'LT':return formatMoment(m,moment.longDateFormat[input]);default:return input.replace(/(^\[)|(\\)|\]$/g,"");}}
return inputString.replace(formattingTokens,replaceFunction);}
function getParseRegexForToken(token){switch(token){case'S':return parseTokenOneDigit;case'SS':return parseTokenTwoDigits;case'SSS':case'DDDD':return parseTokenThreeDigits;case'YYYY':return parseTokenFourDigits;case'DDD':return parseTokenOneToThreeDigits;case'MMM':case'MMMM':case'ddd':case'dddd':case'a':case'A':return parseTokenWord;case'Z':case'ZZ':return parseTokenTimezone;case'T':return parseTokenT;case'MM':case'DD':case'dd':case'YY':case'HH':case'hh':case'mm':case'ss':case'M':case'D':case'd':case'H':case'h':case'm':case's':return parseTokenOneOrTwoDigits;default:return new RegExp(token.replace('\\',''));}}
function addTimeToArrayFromToken(token,input,datePartArray,config){var a;switch(token){case'M':case'MM':datePartArray[1]=~~input-1;break;case'MMM':case'MMMM':for(a=0;a<12;a++){if(moment.monthsParse[a].test(input)){datePartArray[1]=a;break;}}
break;case'D':case'DD':case'DDD':case'DDDD':datePartArray[2]=~~input;break;case'YY':input=~~input;datePartArray[0]=input+(input>70?1900:2000);break;case'YYYY':datePartArray[0]=~~Math.abs(input);break;case'a':case'A':config.isPm=(input.toLowerCase()==='pm');break;case'H':case'HH':case'h':case'hh':datePartArray[3]=~~input;break;case'm':case'mm':datePartArray[4]=~~input;break;case's':case'ss':datePartArray[5]=~~input;break;case'S':datePartArray[6]=~~input*100;break;case'SS':datePartArray[6]=~~input*10;break;case'SSS':datePartArray[6]=~~input;break;case'Z':case'ZZ':config.isUTC=true;a=(input+'').match(parseTimezoneChunker);if(a&&a[1]){config.tzh=~~a[1];}
if(a&&a[2]){config.tzm=~~a[2];}
if(a&&a[0]==='+'){config.tzh=-config.tzh;config.tzm=-config.tzm;}
break;}}
function makeDateFromStringAndFormat(string,format){var datePartArray=[0,0,1,0,0,0,0],config={tzh:0,tzm:0},tokens=format.match(formattingTokens),i,parsedInput;for(i=0;i<tokens.length;i++){parsedInput=(getParseRegexForToken(tokens[i]).exec(string)||[0])[0];string=string.replace(getParseRegexForToken(tokens[i]),'');addTimeToArrayFromToken(tokens[i],parsedInput,datePartArray,config);}
if(config.isPm&&datePartArray[3]<12){datePartArray[3]+=12;}
if(config.isPm===false&&datePartArray[3]===12){datePartArray[3]=0;}
datePartArray[3]+=config.tzh;datePartArray[4]+=config.tzm;return config.isUTC?new Date(Date.UTC.apply({},datePartArray)):dateFromArray(datePartArray);}
function compareArrays(array1,array2){var len=Math.min(array1.length,array2.length),lengthDiff=Math.abs(array1.length-array2.length),diffs=0,i;for(i=0;i<len;i++){if(~~array1[i]!==~~array2[i]){diffs++;}}
return diffs+lengthDiff;}
function makeDateFromStringAndArray(string,formats){var output,inputParts=string.match(parseMultipleFormatChunker),formattedInputParts,scoreToBeat=99,i,currentDate,currentScore;for(i=0;i<formats.length;i++){currentDate=makeDateFromStringAndFormat(string,formats[i]);formattedInputParts=formatMoment(new Moment(currentDate),formats[i]).match(parseMultipleFormatChunker);currentScore=compareArrays(inputParts,formattedInputParts);if(currentScore<scoreToBeat){scoreToBeat=currentScore;output=currentDate;}}
return output;}
function makeDateFromString(string){var format='YYYY-MM-DDT',i;if(isoRegex.exec(string)){for(i=0;i<3;i++){if(isoTimes[i][1].exec(string)){format+=isoTimes[i][0];break;}}
return parseTokenTimezone.exec(string)?makeDateFromStringAndFormat(string,format+' Z'):makeDateFromStringAndFormat(string,format);}
return new Date(string);}
function substituteTimeAgo(string,number,withoutSuffix,isFuture){var rt=moment.relativeTime[string];return(typeof rt==='function')?rt(number||1,!!withoutSuffix,string,isFuture):rt.replace(/%d/i,number||1);}
function relativeTime(milliseconds,withoutSuffix){var seconds=round(Math.abs(milliseconds)/1000),minutes=round(seconds/60),hours=round(minutes/60),days=round(hours/24),years=round(days/365),args=seconds<45&&['s',seconds]||minutes===1&&['m']||minutes<45&&['mm',minutes]||hours===1&&['h']||hours<22&&['hh',hours]||days===1&&['d']||days<=25&&['dd',days]||days<=45&&['M']||days<345&&['MM',round(days/30)]||years===1&&['y']||['yy',years];args[2]=withoutSuffix;args[3]=milliseconds>0;return substituteTimeAgo.apply({},args);}
moment=function(input,format){if(input===null||input===''){return null;}
var date,matched,isUTC;if(moment.isMoment(input)){date=new Date(+input._d);isUTC=input._isUTC;}else if(format){if(isArray(format)){date=makeDateFromStringAndArray(input,format);}else{date=makeDateFromStringAndFormat(input,format);}}else{matched=aspNetJsonRegex.exec(input);date=input===undefined?new Date():matched?new Date(+matched[1]):input instanceof Date?input:isArray(input)?dateFromArray(input):typeof input==='string'?makeDateFromString(input):new Date(input);}
return new Moment(date,isUTC);};moment.utc=function(input,format){if(isArray(input)){return new Moment(new Date(Date.UTC.apply({},input)),true);}
return(format&&input)?moment(input+' +0000',format+' Z').utc():moment(parseTokenTimezone.exec(input)?input:input+'+0000').utc();};moment.unix=function(input){return moment(input*1000);};moment.duration=function(input,key){var isDuration=moment.isDuration(input),isNumber=(typeof input==='number'),duration=(isDuration?input._data:(isNumber?{}:input));if(isNumber){if(key){duration[key]=input;}else{duration.milliseconds=input;}}
return new Duration(duration);};moment.humanizeDuration=function(num,type,withSuffix){return moment.duration(num,type).humanize(withSuffix);};moment.version=VERSION;moment.defaultFormat=isoFormat;moment.lang=function(key,values){var i,req,parse=[];if(!key){return currentLanguage;}
if(values){for(i=0;i<12;i++){parse[i]=new RegExp('^'+values.months[i]+'|^'+values.monthsShort[i].replace('.',''),'i');}
values.monthsParse=values.monthsParse||parse;languages[key]=values;}
if(languages[key]){for(i=0;i<langConfigProperties.length;i++){moment[langConfigProperties[i]]=languages[key][langConfigProperties[i]]||languages.en[langConfigProperties[i]];}
currentLanguage=key;}else{if(hasModule){req=require('./lang/'+key);moment.lang(key,req);}}};moment.lang('en',{months:"January_February_March_April_May_June_July_August_September_October_November_December".split("_"),monthsShort:"Jan_Feb_Mar_Apr_May_Jun_Jul_Aug_Sep_Oct_Nov_Dec".split("_"),weekdays:"Sunday_Monday_Tuesday_Wednesday_Thursday_Friday_Saturday".split("_"),weekdaysShort:"Sun_Mon_Tue_Wed_Thu_Fri_Sat".split("_"),longDateFormat:{LT:"h:mm A",L:"MM/DD/YYYY",LL:"MMMM D YYYY",LLL:"MMMM D YYYY LT",LLLL:"dddd, MMMM D YYYY LT"},meridiem:false,calendar:{sameDay:'[Today at] LT',nextDay:'[Tomorrow at] LT',nextWeek:'dddd [at] LT',lastDay:'[Yesterday at] LT',lastWeek:'[last] dddd [at] LT',sameElse:'L'},relativeTime:{future:"in %s",past:"%s ago",s:"a few seconds",m:"a minute",mm:"%d minutes",h:"an hour",hh:"%d hours",d:"a day",dd:"%d days",M:"a month",MM:"%d months",y:"a year",yy:"%d years"},ordinal:function(number){var b=number%10;return(~~(number%100/10)===1)?'th':(b===1)?'st':(b===2)?'nd':(b===3)?'rd':'th';}});moment.isMoment=function(obj){return obj instanceof Moment;};moment.isDuration=function(obj){return obj instanceof Duration;};moment.fn=Moment.prototype={clone:function(){return moment(this);},valueOf:function(){return+this._d;},unix:function(){return Math.floor(+this._d/1000);},toString:function(){return this._d.toString();},toDate:function(){return this._d;},utc:function(){this._isUTC=true;return this;},local:function(){this._isUTC=false;return this;},format:function(inputString){return formatMoment(this,inputString?inputString:moment.defaultFormat);},add:function(input,val){var dur=val?moment.duration(+val,input):moment.duration(input);addOrSubtractDurationFromMoment(this,dur,1);return this;},subtract:function(input,val){var dur=val?moment.duration(+val,input):moment.duration(input);addOrSubtractDurationFromMoment(this,dur,-1);return this;},diff:function(input,val,asFloat){var inputMoment=this._isUTC?moment(input).utc():moment(input).local(),zoneDiff=(this.zone()-inputMoment.zone())*6e4,diff=this._d-inputMoment._d-zoneDiff,year=this.year()-inputMoment.year(),month=this.month()-inputMoment.month(),date=this.date()-inputMoment.date(),output;if(val==='months'){output=year*12+month+date/30;}else if(val==='years'){output=year+(month+date/30)/12;}else{output=val==='seconds'?diff/1e3:val==='minutes'?diff/6e4:val==='hours'?diff/36e5:val==='days'?diff/864e5:val==='weeks'?diff/6048e5:diff;}
return asFloat?output:round(output);},from:function(time,withoutSuffix){return moment.duration(this.diff(time)).humanize(!withoutSuffix);},fromNow:function(withoutSuffix){return this.from(moment(),withoutSuffix);},calendar:function(){var diff=this.diff(moment().sod(),'days',true),calendar=moment.calendar,allElse=calendar.sameElse,format=diff<-6?allElse:diff<-1?calendar.lastWeek:diff<0?calendar.lastDay:diff<1?calendar.sameDay:diff<2?calendar.nextDay:diff<7?calendar.nextWeek:allElse;return this.format(typeof format==='function'?format.apply(this):format);},isLeapYear:function(){var year=this.year();return(year%4===0&&year%100!==0)||year%400===0;},isDST:function(){return(this.zone()<moment([this.year()]).zone()||this.zone()<moment([this.year(),5]).zone());},day:function(input){var day=this._isUTC?this._d.getUTCDay():this._d.getDay();return input==null?day:this.add({d:input-day});},sod:function(){return this.clone().hours(0).minutes(0).seconds(0).milliseconds(0);},eod:function(){return this.sod().add({d:1,ms:-1});},zone:function(){return this._isUTC?0:this._d.getTimezoneOffset();},daysInMonth:function(){return this.clone().month(this.month()+1).date(0).date();}};function makeGetterAndSetter(name,key){moment.fn[name]=function(input){var utc=this._isUTC?'UTC':'';if(input!=null){this._d['set'+utc+key](input);return this;}else{return this._d['get'+utc+key]();}};}
for(i=0;i<proxyGettersAndSetters.length;i++){makeGetterAndSetter(proxyGettersAndSetters[i].toLowerCase(),proxyGettersAndSetters[i]);}
makeGetterAndSetter('year','FullYear');moment.duration.fn=Duration.prototype={weeks:function(){return absRound(this.days()/7);},valueOf:function(){return this._milliseconds+
this._days*864e5+
this._months*2592e6;},humanize:function(withSuffix){var difference=+this,rel=moment.relativeTime,output=relativeTime(difference,!withSuffix);if(withSuffix){output=(difference<=0?rel.past:rel.future).replace(/%s/i,output);}
return output;}};function makeDurationGetter(name){moment.duration.fn[name]=function(){return this._data[name];};}
function makeDurationAsGetter(name,factor){moment.duration.fn['as'+name]=function(){return+this/factor;};}
for(i=0;i<durationGetters.length;i++){makeDurationGetter(durationGetters[i]);}
for(i in unitMillisecondFactors){if(unitMillisecondFactors.hasOwnProperty(i)){makeDurationAsGetter(i,unitMillisecondFactors[i]);}}
if(hasModule){module.exports=moment;}
if(typeof window!=='undefined'&&typeof ender==='undefined'){window.moment=moment;}
if(typeof define==="function"&&define.amd){define("moment",[],function(){return moment;});}})(Date);(function($){function Placeholder(input){this.input=input;if(input.attr('type')=='password'){this.handlePassword();}
$(input[0].form).submit(function(){if(input.hasClass('placeholder')&&input[0].value==input.attr('placeholder')){input[0].value='';}});}
Placeholder.prototype={show:function(loading){if(this.input[0].value===''||(loading&&this.valueIsPlaceholder())){if(this.isPassword){try{this.input[0].setAttribute('type','text');}catch(e){this.input.before(this.fakePassword.show()).hide();}}
this.input.addClass('placeholder');this.input[0].value=this.input.attr('placeholder');}},hide:function(){if(this.valueIsPlaceholder()&&this.input.hasClass('placeholder')){this.input.removeClass('placeholder');this.input[0].value='';if(this.isPassword){try{this.input[0].setAttribute('type','password');}catch(e){}
this.input.show();this.input[0].focus();}}},valueIsPlaceholder:function(){return this.input[0].value==this.input.attr('placeholder');},handlePassword:function(){var input=this.input;input.attr('realType','password');this.isPassword=true;if($.browser.msie&&input[0].outerHTML){var fakeHTML=$(input[0].outerHTML.replace(/type=(['"])?password\1/gi,'type=$1text$1'));this.fakePassword=fakeHTML.val(input.attr('placeholder')).addClass('placeholder').focus(function(){input.trigger('focus');$(this).hide();});$(input[0].form).submit(function(){fakeHTML.remove();input.show()});}}};var NATIVE_SUPPORT=!!("placeholder"in document.createElement("input"));$.fn.placeholder=function(){return NATIVE_SUPPORT?this:this.each(function(){var input=$(this);var placeholder=new Placeholder(input);placeholder.show(true);input.focus(function(){placeholder.hide();});input.blur(function(){placeholder.show(false);});if($.browser.msie){$(window).load(function(){if(input.val()){input.removeClass("placeholder");}
placeholder.show(true);});input.focus(function(){if(this.value==""){var range=this.createTextRange();range.collapse(true);range.moveStart('character',0);range.select();}});}});}})(jQuery);(function($){"use strict";$(document).ready(function(){$('input[placeholder], textarea[placeholder]').placeholder();});}(jQuery));(function($){"use strict";$(document).ready(function(){var initFacets=function(){$('.oFacetCheckboxList').each(function(){var $facet=$(this),facetName=$facet.data("facet"),checkboxList=$facet.find("li.checkbox input[type=checkbox]"),selectAllCheckbox=$facet.find("#id_facet_"+facetName+"_"),buttonClickHandler,recalculateHandler,input;input=$('<input>').attr({name:facetName,type:'hidden'});$facet.find('input[type=checkbox]').attr("name","");$facet.find('li').removeClass("hidden");$facet.append(input);recalculateHandler=function(){var value,getValue=function(){var result=[],i;if(selectAllCheckbox.is(":checked")){return"";}
for(i=0;i<checkboxList.length;i++){if(checkboxList[i].checked&&checkboxList[i].value){result.push(checkboxList[i].value);}}
result.sort();return result.join(",");};value=getValue();input.val(value);};buttonClickHandler=function(){var $checkbox=$(this),isSelectAll=!$checkbox.val(),isChecked=$checkbox.attr("checked"),allChecked,anyChecked;if(isSelectAll){checkboxList.attr("checked",false);$checkbox.attr("checked",true);}else{selectAllCheckbox.attr("checked",false);allChecked=true;anyChecked=false;checkboxList.each(function(i,el){var checked=el.checked;if(el.value){allChecked=allChecked&&checked;anyChecked=anyChecked||checked;}});if(allChecked){checkboxList.attr("checked",false);selectAllCheckbox.attr("checked",true);}else if(!anyChecked){selectAllCheckbox.attr("checked",true);}}};checkboxList.bind("click",buttonClickHandler);checkboxList.bind("click",recalculateHandler);recalculateHandler();});};initFacets();$(document).bind("o-refreshpage",initFacets);});}(jQuery));(function($){"use strict";$(document).ready(function(){$.fn.selectToUISlider=function(settings){var selects=$(this),options=$.extend({labels:3,tooltip:true,tooltipSrc:'text',labelSrc:'value',sliderOptions:null},settings),j,handleIds,selectOptions,groups,slideCallback,sliderOptions,sliderComponent,scale,inc,increm,values;function isArray(obj){return obj.constructor===Array;}
function ttText(optIndex){return(options.tooltipSrc==='text')?selectOptions[optIndex].text:selectOptions[optIndex].value;}
function leftVal(i){return(i/(selectOptions.length-1)*100).toFixed(2)+'%';}
handleIds=(function(){var tempArr=[];selects.each(function(){tempArr.push('handle_'+$(this).attr('id'));});return tempArr;}());selectOptions=(function(){var opts=[];selects.eq(0).find('option').each(function(){opts.push({value:$(this).attr('value'),text:$(this).text()});});return opts;}());groups=(function(){if(selects.eq(0).find('optgroup').size()>0){var groupedData=[];selects.eq(0).find('optgroup').each(function(i){groupedData[i]={};groupedData[i].label=$(this).attr('label');groupedData[i].options=[];$(this).find('option').each(function(){groupedData[i].options.push({text:$(this).text(),value:$(this).attr('value')});});});return groupedData;}
return null;}());slideCallback=settings.sliderOptions.slide||null;sliderOptions={step:1,min:0,orientation:'horizontal',max:selectOptions.length-1,range:selects.length>1,slide:function(e,ui){var retval=!slideCallback||slideCallback(e,ui),thisHandle=$(ui.handle),textval=ttText(ui.value),currSelect;thisHandle.attr('aria-valuetext',textval).attr('aria-valuenow',ui.value).find('.ui-slider-tooltip .ttContent').text(textval);currSelect=$('#'+thisHandle.attr('id').split('handle_')[1]);currSelect.find('option').eq(ui.value).attr('selected','selected');return retval;},values:(function(){var values=[];selects.each(function(){values.push($(this).get(0).selectedIndex);});return values;}())};delete settings.sliderOptions.slide;options.sliderOptions=(settings)?$.extend(sliderOptions,settings.sliderOptions):sliderOptions;selects.bind('change keyup click',function(){var thisIndex=$(this).get(0).selectedIndex,thisHandle=$('#handle_'+$(this).attr('id')),handleIndex=thisHandle.data('handleNum');thisHandle.parents('.ui-slider:eq(0)').slider("values",handleIndex,thisIndex);});sliderComponent=$('<div></div>');selects.each(function(i){var hidett='',thisLabel=$('label[for='+$(this).attr('id')+']'),labelText=(thisLabel.size()>0)?('Slider control for '+thisLabel.text()):"",thisLabelId=thisLabel.attr('id')||thisLabel.attr('id','label_'+handleIds[i]).attr('id');if(options.tooltip===false){hidett=' style="display: none;"';}
$('<a '+'href="#" tabindex="0" '+'id="'+handleIds[i]+'" '+'class="ui-slider-handle" '+'role="slider" '+'aria-labelledby="'+thisLabelId+'" '+'aria-valuemin="'+options.sliderOptions.min+'" '+'aria-valuemax="'+options.sliderOptions.max+'" '+'aria-valuenow="'+options.sliderOptions.values[i]+'" '+'aria-valuetext="'+ttText(options.sliderOptions.values[i])+'" '+'><span class="screenReaderContext">'+labelText+'</span>'+'<span class="ui-slider-tooltip ui-widget-content ui-corner-all"'+hidett+'><span class="ttContent"></span>'+'<span class="ui-tooltip-pointer-down ui-widget-content"><span class="ui-tooltip-pointer-down-inner"></span></span>'+'</span></a>').data('handleNum',i).appendTo(sliderComponent);});if(groups){inc=0;scale=sliderComponent.append('<dl class="ui-slider-scale ui-helper-reset" role="presentation"></dl>').find('.ui-slider-scale:eq(0)');$(groups).each(function(h){var groupOpts;scale.append('<dt style="width: '+(100/groups.length).toFixed(2)+'%'+'; left:'+(h/(groups.length-1)*100).toFixed(2)+'%'+'"><span>'+this.label+'</span></dt>');groupOpts=this.options;$(this.options).each(function(i){var style=(inc===selectOptions.length-1||inc===0)?'style="display: none;"':'',labelText=(options.labelSrc==='text')?groupOpts[i].text:groupOpts[i].value;scale.append('<dd style="left:'+leftVal(inc)+'"><span class="ui-slider-label">'+labelText+'</span><span class="ui-slider-tic ui-widget-content"'+style+'></span></dd>');inc++;});});}
else{scale=sliderComponent.append('<ol class="ui-slider-scale ui-helper-reset" role="presentation"></ol>').find('.ui-slider-scale:eq(0)');$(selectOptions).each(function(i){var style=(i===selectOptions.length-1||i===0)?'style="display: none;"':'',labelText=(options.labelSrc==='text')?this.text:this.value;scale.append('<li style="left:'+leftVal(i)+'"><span class="ui-slider-label">'+labelText+'</span><span class="ui-slider-tic ui-widget-content"'+style+'></span></li>');});}
if(options.labels>1){sliderComponent.find('.ui-slider-scale li:last span.ui-slider-label, .ui-slider-scale dd:last span.ui-slider-label').addClass('ui-slider-label-show');}
increm=Math.max(1,Math.round(selectOptions.length/options.labels));for(j=0;j<selectOptions.length;j+=increm){if((selectOptions.length-j)>increm){sliderComponent.find('.ui-slider-scale li:eq('+j+') span.ui-slider-label, .ui-slider-scale dd:eq('+j+') span.ui-slider-label').addClass('ui-slider-label-show');}}
sliderComponent.find('.ui-slider-scale dt').each(function(i){$(this).css({'left':((100/(groups.length))*i).toFixed(2)+'%'});});sliderComponent.insertAfter($(this).eq(this.length-1)).slider(options.sliderOptions).attr('role','application').find('.ui-slider-label').each(function(){$(this).css('marginLeft',-$(this).width()/2);});sliderComponent.find('.ui-tooltip-pointer-down-inner').each(function(){var bWidth=$('.ui-tooltip-pointer-down-inner').css('borderTopWidth'),bColor=$(this).parents('.ui-slider-tooltip').css('backgroundColor');$(this).css('border-top',bWidth+' solid '+bColor);});values=sliderComponent.slider('values');if(isArray(values)){$(values).each(function(i){sliderComponent.find('.ui-slider-tooltip .ttContent').eq(i).text(ttText(this));});}
else{sliderComponent.find('.ui-slider-tooltip .ttContent').eq(0).text(ttText(values));}
return this;};});}(jQuery));(function($){"use strict";$(document).ready(function(){var initRangeSelectors=function(){var slideHandler,changeHandler;$('.oRangeSelector').each(function(){var changeTimeout=null,$this=$(this),slideHandler=function(event,ui){var selectElement=$(event.target).parent().find("select:first"),options=selectElement[0].options,i,total=0,startText,endText,resultDiv;if(ui.values[0]>=ui.values[1]){return false;}
for(i=ui.values[0];i<ui.values[1];i++){total+=parseInt($(options[i]).data("count"),10);}
startText=$(options[ui.values[0]]).html();endText=$(options[ui.values[1]]).html();resultDiv=$(event.target).parent().find(".oRangeSelectorResults");resultDiv.find(".oRangeSelectorFrom").text(startText);resultDiv.find(".oRangeSelectorTo").text(endText);resultDiv.find(".oRangeSelectorCount").text(total);return!!total;},changeHandler=function(event,ui){slideHandler(event,ui);$this.find("select").each(function(i){this.selectedIndex=ui.values[i];});$this.find("select:first").trigger("o-external-change");};$(this).find("select").hide().selectToUISlider({labels:0,tooltip:false,sliderOptions:{slide:slideHandler,change:slideHandler,stop:changeHandler}});});$('.oRangeSelector').each(function(){$(this).find("select:first").trigger("change");});};initRangeSelectors();$(document).bind("o-refreshpage",initRangeSelectors);});}(jQuery));(function(factory){if(typeof define==='function'&&define.amd){define(['jquery'],factory);}
else{factory(jQuery);}}
(function($){"use strict";var TRUE=true,FALSE=false,NULL=null,undefined,QTIP,PLUGINS,MOUSE,usedIDs={},uitooltip='ui-tooltip',widget='ui-widget',disabled='ui-state-disabled',selector='div.qtip.'+uitooltip,defaultClass=uitooltip+'-default',focusClass=uitooltip+'-focus',hoverClass=uitooltip+'-hover',fluidClass=uitooltip+'-fluid',hideOffset='-31000px',replaceSuffix='_replacedByqTip',oldtitle='oldtitle',trackingBound;function log(){log.history=log.history||[];log.history.push(arguments);if('object'===typeof console){var c=console[console.warn?'warn':'log'],args=Array.prototype.slice.call(arguments),a;if(typeof arguments[0]==='string'){args[0]='qTip2: '+args[0];}
a=c.apply?c.apply(console,args):c(args);}}
function sanitizeOptions(opts)
{var content;if(!opts||'object'!==typeof opts){return FALSE;}
if(opts.metadata===NULL||'object'!==typeof opts.metadata){opts.metadata={type:opts.metadata};}
if('content'in opts){if(opts.content===NULL||'object'!==typeof opts.content||opts.content.jquery){opts.content={text:opts.content};}
content=opts.content.text||FALSE;if(!$.isFunction(content)&&((!content&&!content.attr)||content.length<1||('object'===typeof content&&!content.jquery))){opts.content.text=FALSE;}
if('title'in opts.content){if(opts.content.title===NULL||'object'!==typeof opts.content.title){opts.content.title={text:opts.content.title};}
content=opts.content.title.text||FALSE;if(!$.isFunction(content)&&((!content&&!content.attr)||content.length<1||('object'===typeof content&&!content.jquery))){opts.content.title.text=FALSE;}}}
if('position'in opts){if(opts.position===NULL||'object'!==typeof opts.position){opts.position={my:opts.position,at:opts.position};}}
if('show'in opts){if(opts.show===NULL||'object'!==typeof opts.show){if(opts.show.jquery){opts.show={target:opts.show};}
else{opts.show={event:opts.show};}}}
if('hide'in opts){if(opts.hide===NULL||'object'!==typeof opts.hide){if(opts.hide.jquery){opts.hide={target:opts.hide};}
else{opts.hide={event:opts.hide};}}}
if('style'in opts){if(opts.style===NULL||'object'!==typeof opts.style){opts.style={classes:opts.style};}}
$.each(PLUGINS,function(){if(this.sanitize){this.sanitize(opts);}});return opts;}
function QTip(target,options,id,attr)
{var self=this,docBody=document.body,tooltipID=uitooltip+'-'+id,isPositioning=0,isDrawing=0,tooltip=$(),namespace='.qtip-'+id,elements,cache;self.id=id;self.rendered=FALSE;self.destroyed=FALSE;self.elements=elements={target:target};self.timers={img:{}};self.options=options;self.checks={};self.plugins={};self.cache=cache={event:{},target:$(),disabled:FALSE,attr:attr,onTarget:FALSE};function convertNotation(notation)
{var i=0,obj,option=options,levels=notation.split('.');while(option=option[levels[i++]]){if(i<levels.length){obj=option;}}
return[obj||options,levels.pop()];}
function setWidget(){var on=options.style.widget;tooltip.toggleClass(widget,on).toggleClass(defaultClass,options.style.def&&!on);elements.content.toggleClass(widget+'-content',on);if(elements.titlebar){elements.titlebar.toggleClass(widget+'-header',on);}
if(elements.button){elements.button.toggleClass(uitooltip+'-icon',!on);}}
function removeTitle(reposition)
{if(elements.title){elements.titlebar.remove();elements.titlebar=elements.title=elements.button=NULL;if(reposition!==FALSE){self.reposition();}}}
function createButton()
{var button=options.content.title.button,isString=typeof button==='string',close=isString?button:'Close tooltip';if(elements.button){elements.button.remove();}
if(button.jquery){elements.button=button;}
else{elements.button=$('<a />',{'class':'ui-state-default ui-tooltip-close '+(options.style.widget?'':uitooltip+'-icon'),'title':close,'aria-label':close}).prepend($('<span />',{'class':'ui-icon ui-icon-close','html':'&times;'}));}
elements.button.appendTo(elements.titlebar).attr('role','button').click(function(event){if(!tooltip.hasClass(disabled)){self.hide(event);}
return FALSE;});self.redraw();}
function createTitle()
{var id=tooltipID+'-title';if(elements.titlebar){removeTitle();}
elements.titlebar=$('<div />',{'class':uitooltip+'-titlebar '+(options.style.widget?'ui-widget-header':'')}).append(elements.title=$('<div />',{'id':id,'class':uitooltip+'-title','aria-atomic':TRUE})).insertBefore(elements.content).delegate('.ui-tooltip-close','mousedown keydown mouseup keyup mouseout',function(event){$(this).toggleClass('ui-state-active ui-state-focus',event.type.substr(-4)==='down');}).delegate('.ui-tooltip-close','mouseover mouseout',function(event){$(this).toggleClass('ui-state-hover',event.type==='mouseover');});if(options.content.title.button){createButton();}
else if(self.rendered){self.redraw();}}
function updateButton(button)
{var elem=elements.button,title=elements.title;if(!self.rendered){return FALSE;}
if(!button){elem.remove();}
else{if(!title){createTitle();}
createButton();}}
function updateTitle(content,reposition)
{var elem=elements.title;if(!self.rendered||!content){return FALSE;}
if($.isFunction(content)){content=content.call(target,cache.event,self);}
if(content===FALSE||(!content&&content!=='')){return removeTitle(FALSE);}
else if(content.jquery&&content.length>0){elem.empty().append(content.css({display:'block'}));}
else{elem.html(content);}
self.redraw();if(reposition!==FALSE&&self.rendered&&tooltip[0].offsetWidth>0){self.reposition(cache.event);}}
function updateContent(content,reposition)
{var elem=elements.content;if(!self.rendered||!content){return FALSE;}
if($.isFunction(content)){content=content.call(target,cache.event,self)||'';}
if(content.jquery&&content.length>0){elem.empty().append(content.css({display:'block'}));}
else{elem.html(content);}
function detectImages(next){var images,srcs={};function imageLoad(image){if(image){delete srcs[image.src];clearTimeout(self.timers.img[image.src]);$(image).unbind(namespace);}
if($.isEmptyObject(srcs)){self.redraw();if(reposition!==FALSE){self.reposition(cache.event);}
next();}}
if((images=elem.find('img[src]:not([height]):not([width])')).length===0){return imageLoad();}
images.each(function(i,elem){if(srcs[elem.src]!==undefined){return;}
var iterations=0,maxIterations=3;(function timer(){if(elem.height||elem.width||(iterations>maxIterations)){return imageLoad(elem);}
iterations+=1;self.timers.img[elem.src]=setTimeout(timer,700);}());$(elem).bind('error'+namespace+' load'+namespace,function(){imageLoad(this);});srcs[elem.src]=elem;});}
if(self.rendered<0){tooltip.queue('fx',detectImages);}
else{isDrawing=0;detectImages($.noop);}
return self;}
function assignEvents()
{var posOptions=options.position,targets={show:options.show.target,hide:options.hide.target,viewport:$(posOptions.viewport),document:$(document),body:$(document.body),window:$(window)},events={show:$.trim(''+options.show.event).split(' '),hide:$.trim(''+options.hide.event).split(' ')},IE6=$.browser.msie&&parseInt($.browser.version,10)===6;function showMethod(event)
{if(tooltip.hasClass(disabled)){return FALSE;}
clearTimeout(self.timers.show);clearTimeout(self.timers.hide);var callback=function(){self.toggle(TRUE,event);};if(options.show.delay>0){self.timers.show=setTimeout(callback,options.show.delay);}
else{callback();}}
function hideMethod(event)
{if(tooltip.hasClass(disabled)||isPositioning||isDrawing){return FALSE;}
var relatedTarget=$(event.relatedTarget||event.target),ontoTooltip=relatedTarget.closest(selector)[0]===tooltip[0],ontoTarget=relatedTarget[0]===targets.show[0];clearTimeout(self.timers.show);clearTimeout(self.timers.hide);if((posOptions.target==='mouse'&&ontoTooltip)||(options.hide.fixed&&((/mouse(out|leave|move)/).test(event.type)&&(ontoTooltip||ontoTarget)))){try{event.preventDefault();event.stopImmediatePropagation();}catch(e){}return;}
if(options.hide.delay>0){self.timers.hide=setTimeout(function(){self.hide(event);},options.hide.delay);}
else{self.hide(event);}}
function inactiveMethod(event)
{if(tooltip.hasClass(disabled)){return FALSE;}
clearTimeout(self.timers.inactive);self.timers.inactive=setTimeout(function(){self.hide(event);},options.hide.inactive);}
function repositionMethod(event){if(self.rendered&&tooltip[0].offsetWidth>0){self.reposition(event);}}
tooltip.bind('mouseenter'+namespace+' mouseleave'+namespace,function(event){var state=event.type==='mouseenter';if(state){self.focus(event);}
tooltip.toggleClass(hoverClass,state);});if(options.hide.fixed){targets.hide=targets.hide.add(tooltip);tooltip.bind('mouseover'+namespace,function(){if(!tooltip.hasClass(disabled)){clearTimeout(self.timers.hide);}});}
if(/mouse(out|leave)/i.test(options.hide.event)){if(options.hide.leave==='window'){targets.window.bind('mouseout'+namespace+' blur'+namespace,function(event){if(/select|option/.test(event.target)&&!event.relatedTarget){self.hide(event);}});}}
else if(/mouse(over|enter)/i.test(options.show.event)){targets.hide.bind('mouseleave'+namespace,function(event){clearTimeout(self.timers.show);});}
if((''+options.hide.event).indexOf('unfocus')>-1){posOptions.container.closest('html').bind('mousedown'+namespace,function(event){var elem=$(event.target),enabled=self.rendered&&!tooltip.hasClass(disabled)&&tooltip[0].offsetWidth>0,isAncestor=elem.parents(selector).filter(tooltip[0]).length>0;if(elem[0]!==target[0]&&elem[0]!==tooltip[0]&&!isAncestor&&!target.has(elem[0]).length&&!elem.attr('disabled')){self.hide(event);}});}
if('number'===typeof options.hide.inactive){targets.show.bind('qtip-'+id+'-inactive',inactiveMethod);$.each(QTIP.inactiveEvents,function(index,type){targets.hide.add(elements.tooltip).bind(type+namespace+'-inactive',inactiveMethod);});}
$.each(events.hide,function(index,type){var showIndex=$.inArray(type,events.show),targetHide=$(targets.hide);if((showIndex>-1&&targetHide.add(targets.show).length===targetHide.length)||type==='unfocus')
{targets.show.bind(type+namespace,function(event){if(tooltip[0].offsetWidth>0){hideMethod(event);}
else{showMethod(event);}});delete events.show[showIndex];}
else{targets.hide.bind(type+namespace,hideMethod);}});$.each(events.show,function(index,type){targets.show.bind(type+namespace,showMethod);});if('number'===typeof options.hide.distance){targets.show.add(tooltip).bind('mousemove'+namespace,function(event){var origin=cache.origin||{},limit=options.hide.distance,abs=Math.abs;if(abs(event.pageX-origin.pageX)>=limit||abs(event.pageY-origin.pageY)>=limit){self.hide(event);}});}
if(posOptions.target==='mouse'){targets.show.bind('mousemove'+namespace,function(event){MOUSE={pageX:event.pageX,pageY:event.pageY,type:'mousemove'};});if(posOptions.adjust.mouse){if(options.hide.event){tooltip.bind('mouseleave'+namespace,function(event){if((event.relatedTarget||event.target)!==targets.show[0]){self.hide(event);}});elements.target.bind('mouseenter'+namespace+' mouseleave'+namespace,function(event){cache.onTarget=event.type==='mouseenter';});}
targets.document.bind('mousemove'+namespace,function(event){if(self.rendered&&cache.onTarget&&!tooltip.hasClass(disabled)&&tooltip[0].offsetWidth>0){self.reposition(event||MOUSE);}});}}
if(posOptions.adjust.resize||targets.viewport.length){($.event.special.resize?targets.viewport:targets.window).bind('resize'+namespace,repositionMethod);}
if(targets.viewport.length||(IE6&&tooltip.css('position')==='fixed')){targets.viewport.bind('scroll'+namespace,repositionMethod);}}
function unassignEvents()
{var targets=[options.show.target[0],options.hide.target[0],self.rendered&&elements.tooltip[0],options.position.container[0],options.position.viewport[0],window,document];if(self.rendered){$([]).pushStack($.grep(targets,function(i){return typeof i==='object';})).unbind(namespace);}
else{options.show.target.unbind(namespace+'-create');}}
self.checks.builtin={'^id$':function(obj,o,v){var id=v===TRUE?QTIP.nextid:v,tooltipID=uitooltip+'-'+id;if(id!==FALSE&&id.length>0&&!$('#'+tooltipID).length){tooltip[0].id=tooltipID;elements.content[0].id=tooltipID+'-content';elements.title[0].id=tooltipID+'-title';}},'^content.text$':function(obj,o,v){updateContent(v);},'^content.title.text$':function(obj,o,v){if(!v){return removeTitle();}
if(!elements.title&&v){createTitle();}
updateTitle(v);},'^content.title.button$':function(obj,o,v){updateButton(v);},'^position.(my|at)$':function(obj,o,v){if('string'===typeof v){obj[o]=new PLUGINS.Corner(v);}},'^position.container$':function(obj,o,v){if(self.rendered){tooltip.appendTo(v);}},'^show.ready$':function(){if(!self.rendered){self.render(1);}
else{self.toggle(TRUE);}},'^style.classes$':function(obj,o,v){tooltip.attr('class',uitooltip+' qtip ui-helper-reset '+v);},'^style.widget|content.title':setWidget,'^events.(render|show|move|hide|focus|blur)$':function(obj,o,v){tooltip[($.isFunction(v)?'':'un')+'bind']('tooltip'+o,v);},'^(show|hide|position).(event|target|fixed|inactive|leave|distance|viewport|adjust)':function(){var posOptions=options.position;tooltip.attr('tracking',posOptions.target==='mouse'&&posOptions.adjust.mouse);unassignEvents();assignEvents();}};$.extend(self,{render:function(show)
{if(self.rendered){return self;}
var text=options.content.text,title=options.content.title.text,posOptions=options.position,callback=$.Event('tooltiprender');$.attr(target[0],'aria-describedby',tooltipID);tooltip=elements.tooltip=$('<div/>',{'id':tooltipID,'class':uitooltip+' qtip ui-helper-reset '+defaultClass+' '+options.style.classes+' '+uitooltip+'-pos-'+options.position.my.abbrev(),'width':options.style.width||'','height':options.style.height||'','tracking':posOptions.target==='mouse'&&posOptions.adjust.mouse,'role':'alert','aria-live':'polite','aria-atomic':FALSE,'aria-describedby':tooltipID+'-content','aria-hidden':TRUE}).toggleClass(disabled,cache.disabled).data('qtip',self).appendTo(options.position.container).append(elements.content=$('<div />',{'class':uitooltip+'-content','id':tooltipID+'-content','aria-atomic':TRUE}));self.rendered=-1;isDrawing=1;isPositioning=1;if(title){createTitle();if(!$.isFunction(title)){updateTitle(title,FALSE);}}
if(!$.isFunction(text)){updateContent(text,FALSE);}
self.rendered=TRUE;setWidget();$.each(options.events,function(name,callback){if($.isFunction(callback)){tooltip.bind(name==='toggle'?'tooltipshow tooltiphide':'tooltip'+name,callback);}});$.each(PLUGINS,function(){if(this.initialize==='render'){this(self);}});assignEvents();tooltip.queue('fx',function(next){callback.originalEvent=cache.event;tooltip.trigger(callback,[self]);isDrawing=0;isPositioning=0;self.redraw();if(options.show.ready||show){self.toggle(TRUE,cache.event,FALSE);}
next();});return self;},get:function(notation)
{var result,o;switch(notation.toLowerCase())
{case'dimensions':result={height:tooltip.outerHeight(),width:tooltip.outerWidth()};break;case'offset':result=PLUGINS.offset(tooltip,options.position.container);break;default:o=convertNotation(notation.toLowerCase());result=o[0][o[1]];result=result.precedance?result.string():result;break;}
return result;},set:function(option,value)
{var rmove=/^position\.(my|at|adjust|target|container)|style|content|show\.ready/i,rdraw=/^content\.(title|attr)|style/i,reposition=FALSE,redraw=FALSE,checks=self.checks,name;function callback(notation,args){var category,rule,match;for(category in checks){for(rule in checks[category]){if(match=(new RegExp(rule,'i')).exec(notation)){args.push(match);checks[category][rule].apply(self,args);}}}}
if('string'===typeof option){name=option;option={};option[name]=value;}
else{option=$.extend(TRUE,{},option);}
$.each(option,function(notation,value){var obj=convertNotation(notation.toLowerCase()),previous;previous=obj[0][obj[1]];obj[0][obj[1]]='object'===typeof value&&value.nodeType?$(value):value;option[notation]=[obj[0],obj[1],value,previous];reposition=rmove.test(notation)||reposition;redraw=rdraw.test(notation)||redraw;});sanitizeOptions(options);isPositioning=isDrawing=1;$.each(option,callback);isPositioning=isDrawing=0;if(self.rendered&&tooltip[0].offsetWidth>0){if(reposition){self.reposition(options.position.target==='mouse'?NULL:cache.event);}
if(redraw){self.redraw();}}
return self;},toggle:function(state,event)
{if(!self.rendered){return state?self.render(1):self;}
var type=state?'show':'hide',opts=options[type],otherOpts=options[!state?'show':'hide'],posOptions=options.position,contentOptions=options.content,visible=tooltip[0].offsetWidth>0,animate=state||opts.target.length===1,sameTarget=!event||opts.target.length<2||cache.target[0]===event.target,delay,callback;if((typeof state).search('boolean|number')){state=!visible;}
if(!tooltip.is(':animated')&&visible===state&&sameTarget){return self;}
if(event){if((/over|enter/).test(event.type)&&(/out|leave/).test(cache.event.type)&&options.show.target.add(event.target).length===options.show.target.length&&tooltip.has(event.relatedTarget).length){return self;}
cache.event=$.extend({},event);}
callback=$.Event('tooltip'+type);callback.originalEvent=event?cache.event:NULL;tooltip.trigger(callback,[self,90]);if(callback.isDefaultPrevented()){return self;}
$.attr(tooltip[0],'aria-hidden',!!!state);if(state){cache.origin=$.extend({},MOUSE);self.focus(event);if($.isFunction(contentOptions.text)){updateContent(contentOptions.text,FALSE);}
if($.isFunction(contentOptions.title.text)){updateTitle(contentOptions.title.text,FALSE);}
if(!trackingBound&&posOptions.target==='mouse'&&posOptions.adjust.mouse){$(document).bind('mousemove.qtip',function(event){MOUSE={pageX:event.pageX,pageY:event.pageY,type:'mousemove'};});trackingBound=TRUE;}
self.reposition(event,arguments[2]);if((callback.solo=!!opts.solo)){$(selector,opts.solo).not(tooltip).qtip('hide',callback);}}
else{clearTimeout(self.timers.show);delete cache.origin;if(trackingBound&&!$(selector+'[tracking="true"]:visible',opts.solo).not(tooltip).length){$(document).unbind('mousemove.qtip');trackingBound=FALSE;}
self.blur(event);}
function after(){if(state){if($.browser.msie){tooltip[0].style.removeAttribute('filter');}
tooltip.css('overflow','');if('string'===typeof opts.autofocus){$(opts.autofocus,tooltip).focus();}
opts.target.trigger('qtip-'+id+'-inactive');}
else{tooltip.css({display:'',visibility:'',opacity:'',left:'',top:''});}
callback=$.Event('tooltip'+(state?'visible':'hidden'));callback.originalEvent=event?cache.event:NULL;tooltip.trigger(callback,[self]);}
if(opts.effect===FALSE||animate===FALSE){tooltip[type]();after.call(tooltip);}
else if($.isFunction(opts.effect)){tooltip.stop(1,1);opts.effect.call(tooltip,self);tooltip.queue('fx',function(n){after();n();});}
else{tooltip.fadeTo(90,state?1:0,after);}
if(state){opts.target.trigger('qtip-'+id+'-inactive');}
return self;},show:function(event){return self.toggle(TRUE,event);},hide:function(event){return self.toggle(FALSE,event);},focus:function(event)
{if(!self.rendered){return self;}
var qtips=$(selector),curIndex=parseInt(tooltip[0].style.zIndex,10),newIndex=QTIP.zindex+qtips.length,cachedEvent=$.extend({},event),focusedElem,callback;if(!tooltip.hasClass(focusClass))
{callback=$.Event('tooltipfocus');callback.originalEvent=cachedEvent;tooltip.trigger(callback,[self,newIndex]);if(!callback.isDefaultPrevented()){if(curIndex!==newIndex){qtips.each(function(){if(this.style.zIndex>curIndex){this.style.zIndex=this.style.zIndex-1;}});qtips.filter('.'+focusClass).qtip('blur',cachedEvent);}
tooltip.addClass(focusClass)[0].style.zIndex=newIndex;}}
return self;},blur:function(event){var cachedEvent=$.extend({},event),callback;tooltip.removeClass(focusClass);callback=$.Event('tooltipblur');callback.originalEvent=cachedEvent;tooltip.trigger(callback,[self]);return self;},reposition:function(event,effect)
{if(!self.rendered||isPositioning){return self;}
isPositioning=1;var target=options.position.target,posOptions=options.position,my=posOptions.my,at=posOptions.at,adjust=posOptions.adjust,method=adjust.method.split(' '),elemWidth=tooltip.outerWidth(),elemHeight=tooltip.outerHeight(),targetWidth=0,targetHeight=0,callback=$.Event('tooltipmove'),fixed=tooltip.css('position')==='fixed',viewport=posOptions.viewport,position={left:0,top:0},container=posOptions.container,flipoffset=FALSE,tip=self.plugins.tip,visible=tooltip[0].offsetWidth>0,readjust={horizontal:method[0],vertical:(method[1]=method[1]||method[0]),enabled:viewport.jquery&&target[0]!==window&&target[0]!==docBody&&adjust.method!=='none',left:function(posLeft){var isShift=readjust.horizontal==='shift',adjustx=adjust.x*(readjust.horizontal.substr(-6)==='invert'?2:0),viewportScroll=-container.offset.left+viewport.offset.left+viewport.scrollLeft,myWidth=my.x==='left'?elemWidth:my.x==='right'?-elemWidth:-elemWidth/2,atWidth=at.x==='left'?targetWidth:at.x==='right'?-targetWidth:-targetWidth/2,tipWidth=tip&&tip.size?tip.size.width||0:0,tipAdjust=tip&&tip.corner&&tip.corner.precedance==='x'&&!isShift?tipWidth:0,overflowLeft=viewportScroll-posLeft+tipAdjust,overflowRight=posLeft+elemWidth-viewport.width-viewportScroll+tipAdjust,offset=myWidth-(my.precedance==='x'||my.x===my.y?atWidth:0)-(at.x==='center'?targetWidth/2:0),isCenter=my.x==='center';if(isShift){tipAdjust=tip&&tip.corner&&tip.corner.precedance==='y'?tipWidth:0;offset=(my.x==='left'?1:-1)*myWidth-tipAdjust;position.left+=overflowLeft>0?overflowLeft:overflowRight>0?-overflowRight:0;position.left=Math.max(-container.offset.left+viewport.offset.left+(tipAdjust&&tip.corner.x==='center'?tip.offset:0),posLeft-offset,Math.min(Math.max(-container.offset.left+viewport.offset.left+viewport.width,posLeft+offset),position.left));}
else{if(overflowLeft>0&&(my.x!=='left'||overflowRight>0)){position.left-=offset+adjustx;}
else if(overflowRight>0&&(my.x!=='right'||overflowLeft>0)){position.left-=(isCenter?-offset:offset)+adjustx;}
if(position.left<viewportScroll&&-position.left>overflowRight){position.left=posLeft;}}
return position.left-posLeft;},top:function(posTop){var isShift=readjust.vertical==='shift',adjusty=adjust.y*(readjust.vertical.substr(-6)==='invert'?2:0),viewportScroll=-container.offset.top+viewport.offset.top+viewport.scrollTop,myHeight=my.y==='top'?elemHeight:my.y==='bottom'?-elemHeight:-elemHeight/2,atHeight=at.y==='top'?targetHeight:at.y==='bottom'?-targetHeight:-targetHeight/2,tipHeight=tip&&tip.size?tip.size.height||0:0,tipAdjust=tip&&tip.corner&&tip.corner.precedance==='y'&&!isShift?tipHeight:0,overflowTop=viewportScroll-posTop+tipAdjust,overflowBottom=posTop+elemHeight-viewport.height-viewportScroll+tipAdjust,offset=myHeight-(my.precedance==='y'||my.x===my.y?atHeight:0)-(at.y==='center'?targetHeight/2:0),isCenter=my.y==='center';if(isShift){tipAdjust=tip&&tip.corner&&tip.corner.precedance==='x'?tipHeight:0;offset=(my.y==='top'?1:-1)*myHeight-tipAdjust;position.top+=overflowTop>0?overflowTop:overflowBottom>0?-overflowBottom:0;position.top=Math.max(-container.offset.top+viewport.offset.top+(tipAdjust&&tip.corner.x==='center'?tip.offset:0),posTop-offset,Math.min(Math.max(-container.offset.top+viewport.offset.top+viewport.height,posTop+offset),position.top));}
else{if(overflowTop>0&&(my.y!=='top'||overflowBottom>0)){position.top-=offset+adjusty;}
else if(overflowBottom>0&&(my.y!=='bottom'||overflowTop>0)){position.top-=(isCenter?-offset:offset)+adjusty;}
if(position.top<0&&-position.top>overflowBottom){position.top=posTop;}}
return position.top-posTop;}},win;if($.isArray(target)&&target.length===2){at={x:'left',y:'top'};position={left:target[0],top:target[1]};}
else if(target==='mouse'&&((event&&event.pageX)||cache.event.pageX)){at={x:'left',y:'top'};event=(event&&(event.type==='resize'||event.type==='scroll')?cache.event:event&&event.pageX&&event.type==='mousemove'?event:MOUSE&&MOUSE.pageX&&(adjust.mouse||!event||!event.pageX)?{pageX:MOUSE.pageX,pageY:MOUSE.pageY}:!adjust.mouse&&cache.origin&&cache.origin.pageX&&options.show.distance?cache.origin:event)||event||cache.event||MOUSE||{};position={top:event.pageY,left:event.pageX};}
else{if(target==='event'){if(event&&event.target&&event.type!=='scroll'&&event.type!=='resize'){target=cache.target=$(event.target);}
else{target=cache.target;}}
else{target=cache.target=$(target.jquery?target:elements.target);}
target=$(target).eq(0);if(target.length===0){return self;}
else if(target[0]===document||target[0]===window){targetWidth=PLUGINS.iOS?window.innerWidth:target.width();targetHeight=PLUGINS.iOS?window.innerHeight:target.height();if(target[0]===window){position={top:(viewport||target).scrollTop(),left:(viewport||target).scrollLeft()};}}
else if(target.is('area')&&PLUGINS.imagemap){position=PLUGINS.imagemap(target,at,readjust.enabled?method:FALSE);}
else if(target[0].namespaceURI==='http://www.w3.org/2000/svg'&&PLUGINS.svg){position=PLUGINS.svg(target,at);}
else{targetWidth=target.outerWidth();targetHeight=target.outerHeight();position=PLUGINS.offset(target,container);}
if(position.offset){targetWidth=position.width;targetHeight=position.height;flipoffset=position.flipoffset;position=position.offset;}
if((PLUGINS.iOS<4.1&&PLUGINS.iOS>3.1)||PLUGINS.iOS==4.3||(!PLUGINS.iOS&&fixed)){win=$(window);position.left-=win.scrollLeft();position.top-=win.scrollTop();}
position.left+=at.x==='right'?targetWidth:at.x==='center'?targetWidth/2:0;position.top+=at.y==='bottom'?targetHeight:at.y==='center'?targetHeight/2:0;}
position.left+=adjust.x+(my.x==='right'?-elemWidth:my.x==='center'?-elemWidth/2:0);position.top+=adjust.y+(my.y==='bottom'?-elemHeight:my.y==='center'?-elemHeight/2:0);if(readjust.enabled){viewport={elem:viewport,height:viewport[(viewport[0]===window?'h':'outerH')+'eight'](),width:viewport[(viewport[0]===window?'w':'outerW')+'idth'](),scrollLeft:fixed?0:viewport.scrollLeft(),scrollTop:fixed?0:viewport.scrollTop(),offset:viewport.offset()||{left:0,top:0}};container={elem:container,scrollLeft:container.scrollLeft(),scrollTop:container.scrollTop(),offset:container.offset()||{left:0,top:0}};position.adjusted={left:readjust.horizontal!=='none'?readjust.left(position.left):0,top:readjust.vertical!=='none'?readjust.top(position.top):0};if(position.adjusted.left+position.adjusted.top){tooltip.attr('class',tooltip[0].className.replace(/ui-tooltip-pos-\w+/i,uitooltip+'-pos-'+my.abbrev()));}
if(flipoffset&&position.adjusted.left){position.left+=flipoffset.left;}
if(flipoffset&&position.adjusted.top){position.top+=flipoffset.top;}}
else{position.adjusted={left:0,top:0};}
callback.originalEvent=$.extend({},event);tooltip.trigger(callback,[self,position,viewport.elem||viewport]);if(callback.isDefaultPrevented()){return self;}
delete position.adjusted;if(effect===FALSE||!visible||isNaN(position.left)||isNaN(position.top)||target==='mouse'||!$.isFunction(posOptions.effect)){tooltip.css(position);}
else if($.isFunction(posOptions.effect)){posOptions.effect.call(tooltip,self,$.extend({},position));tooltip.queue(function(next){$(this).css({opacity:'',height:''});if($.browser.msie){this.style.removeAttribute('filter');}
next();});}
isPositioning=0;return self;},redraw:function()
{if(self.rendered<1||isDrawing){return self;}
var container=options.position.container,perc,width,max,min;isDrawing=1;if(options.style.height){tooltip.css('height',options.style.height);}
if(options.style.width){tooltip.css('width',options.style.width);}
else{tooltip.css('width','').addClass(fluidClass);width=tooltip.width()+1;max=tooltip.css('max-width')||'';min=tooltip.css('min-width')||'';perc=(max+min).indexOf('%')>-1?container.width()/100:0;max=((max.indexOf('%')>-1?perc:1)*parseInt(max,10))||width;min=((min.indexOf('%')>-1?perc:1)*parseInt(min,10))||0;width=max+min?Math.min(Math.max(width,min),max):width;tooltip.css('width',Math.round(width)).removeClass(fluidClass);}
isDrawing=0;return self;},disable:function(state)
{if('boolean'!==typeof state){state=!(tooltip.hasClass(disabled)||cache.disabled);}
if(self.rendered){tooltip.toggleClass(disabled,state);$.attr(tooltip[0],'aria-disabled',state);}
else{cache.disabled=!!state;}
return self;},enable:function(){return self.disable(FALSE);},destroy:function()
{var t=target[0],title=$.attr(t,oldtitle),elemAPI=target.data('qtip');self.destroyed=TRUE;if(self.rendered){tooltip.stop(1,0).remove();$.each(self.plugins,function(){if(this.destroy){this.destroy();}});}
clearTimeout(self.timers.show);clearTimeout(self.timers.hide);unassignEvents();if(!elemAPI||self===elemAPI){$.removeData(t,'qtip');if(options.suppress&&title){$.attr(t,'title',title);target.removeAttr(oldtitle);}
target.removeAttr('aria-describedby');}
target.unbind('.qtip-'+id);delete usedIDs[self.id];return target;}});}
function init(id,opts)
{var obj,posOptions,attr,config,title,elem=$(this),docBody=$(document.body),newTarget=this===document?docBody:elem,metadata=(elem.metadata)?elem.metadata(opts.metadata):NULL,metadata5=opts.metadata.type==='html5'&&metadata?metadata[opts.metadata.name]:NULL,html5=elem.data(opts.metadata.name||'qtipopts');try{html5=typeof html5==='string'?(new Function("return "+html5))():html5;}
catch(e){log('Unable to parse HTML5 attribute data: '+html5);}
config=$.extend(TRUE,{},QTIP.defaults,opts,typeof html5==='object'?sanitizeOptions(html5):NULL,sanitizeOptions(metadata5||metadata));posOptions=config.position;config.id=id;if('boolean'===typeof config.content.text){attr=elem.attr(config.content.attr);if(config.content.attr!==FALSE&&attr){config.content.text=attr;}
else{log('Unable to locate content for tooltip! Aborting render of tooltip on element: ',elem);return FALSE;}}
if(!posOptions.container.length){posOptions.container=docBody;}
if(posOptions.target===FALSE){posOptions.target=newTarget;}
if(config.show.target===FALSE){config.show.target=newTarget;}
if(config.show.solo===TRUE){config.show.solo=posOptions.container.closest('body');}
if(config.hide.target===FALSE){config.hide.target=newTarget;}
if(config.position.viewport===TRUE){config.position.viewport=posOptions.container;}
posOptions.container=posOptions.container.eq(0);posOptions.at=new PLUGINS.Corner(posOptions.at);posOptions.my=new PLUGINS.Corner(posOptions.my);if($.data(this,'qtip')){if(config.overwrite){elem.qtip('destroy');}
else if(config.overwrite===FALSE){return FALSE;}}
if(config.suppress&&(title=$.attr(this,'title'))){$(this).removeAttr('title').attr(oldtitle,title);}
obj=new QTip(elem,config,id,!!attr);$.data(this,'qtip',obj);elem.bind('remove.qtip-'+id+' removeqtip.qtip-'+id,function(){obj.destroy();});return obj;}
QTIP=$.fn.qtip=function(options,notation,newValue)
{var command=(''+options).toLowerCase(),returned=NULL,args=$.makeArray(arguments).slice(1),event=args[args.length-1],opts=this[0]?$.data(this[0],'qtip'):NULL;if((!arguments.length&&opts)||command==='api'){return opts;}
else if('string'===typeof options)
{this.each(function()
{var api=$.data(this,'qtip');if(!api){return TRUE;}
if(event&&event.timeStamp){api.cache.event=event;}
if((command==='option'||command==='options')&&notation){if($.isPlainObject(notation)||newValue!==undefined){api.set(notation,newValue);}
else{returned=api.get(notation);return FALSE;}}
else if(api[command]){api[command].apply(api[command],args);}});return returned!==NULL?returned:this;}
else if('object'===typeof options||!arguments.length)
{opts=sanitizeOptions($.extend(TRUE,{},options));return QTIP.bind.call(this,opts,event);}};QTIP.bind=function(opts,event)
{return this.each(function(i){var options,targets,events,namespace,api,id;id=$.isArray(opts.id)?opts.id[i]:opts.id;id=!id||id===FALSE||id.length<1||usedIDs[id]?QTIP.nextid++:(usedIDs[id]=id);namespace='.qtip-'+id+'-create';api=init.call(this,id,opts);if(api===FALSE){return TRUE;}
options=api.options;$.each(PLUGINS,function(){if(this.initialize==='initialize'){this(api);}});targets={show:options.show.target,hide:options.hide.target};events={show:$.trim(''+options.show.event).replace(/ /g,namespace+' ')+namespace,hide:$.trim(''+options.hide.event).replace(/ /g,namespace+' ')+namespace};if(/mouse(over|enter)/i.test(events.show)&&!/mouse(out|leave)/i.test(events.hide)){events.hide+=' mouseleave'+namespace;}
targets.show.bind('mousemove'+namespace,function(event){MOUSE={pageX:event.pageX,pageY:event.pageY,type:'mousemove'};api.cache.onTarget=TRUE;});function hoverIntent(event){function render(){api.render(typeof event==='object'||options.show.ready);targets.show.add(targets.hide).unbind(namespace);}
if(api.cache.disabled){return FALSE;}
api.cache.event=$.extend({},event);api.cache.target=event?$(event.target):[undefined];if(options.show.delay>0){clearTimeout(api.timers.show);api.timers.show=setTimeout(render,options.show.delay);if(events.show!==events.hide){targets.hide.bind(events.hide,function(){clearTimeout(api.timers.show);});}}
else{render();}}
targets.show.bind(events.show,hoverIntent);if(options.show.ready||options.prerender){hoverIntent(event);}});};PLUGINS=QTIP.plugins={Corner:function(corner){corner=(''+corner).replace(/([A-Z])/,' $1').replace(/middle/gi,'center').toLowerCase();this.x=(corner.match(/left|right/i)||corner.match(/center/)||['inherit'])[0].toLowerCase();this.y=(corner.match(/top|bottom|center/i)||['inherit'])[0].toLowerCase();var f=corner.charAt(0);this.precedance=(f==='t'||f==='b'?'y':'x');this.string=function(){return this.precedance==='y'?this.y+this.x:this.x+this.y;};this.abbrev=function(){var x=this.x.substr(0,1),y=this.y.substr(0,1);return x===y?x:(x==='c'||(x!=='c'&&y!=='c'))?y+x:x+y;};this.clone=function(){return{x:this.x,y:this.y,precedance:this.precedance,string:this.string,abbrev:this.abbrev,clone:this.clone};};},offset:function(elem,container){var pos=elem.offset(),docBody=elem.closest('body')[0],parent=container,scrolled,coffset,overflow;function scroll(e,i){pos.left+=i*e.scrollLeft();pos.top+=i*e.scrollTop();}
if(parent){do{if(parent.css('position')!=='static'){coffset=parent.position();pos.left-=coffset.left+(parseInt(parent.css('borderLeftWidth'),10)||0)+(parseInt(parent.css('marginLeft'),10)||0);pos.top-=coffset.top+(parseInt(parent.css('borderTopWidth'),10)||0)+(parseInt(parent.css('marginTop'),10)||0);if(!scrolled&&(overflow=parent.css('overflow'))!=='hidden'&&overflow!=='visible'){scrolled=parent;}}}
while((parent=$(parent[0].offsetParent)).length);if(scrolled&&scrolled[0]!==docBody){scroll(scrolled,1);}}
return pos;},iOS:parseFloat((''+(/CPU.*OS ([0-9_]{1,3})|(CPU like).*AppleWebKit.*Mobile/i.exec(navigator.userAgent)||[0,''])[1]).replace('undefined','3_2').replace('_','.'))||FALSE,fn:{attr:function(attr,val){if(this.length){var self=this[0],title='title',api=$.data(self,'qtip');if(attr===title&&api&&'object'===typeof api&&api.options.suppress){if(arguments.length<2){return $.attr(self,oldtitle);}
else{if(api&&api.options.content.attr===title&&api.cache.attr){api.set('content.text',val);}
return this.attr(oldtitle,val);}}}
return $.fn['attr'+replaceSuffix].apply(this,arguments);},clone:function(keepData){var titles=$([]),title='title',elems=$.fn['clone'+replaceSuffix].apply(this,arguments);if(!keepData){elems.filter('['+oldtitle+']').attr('title',function(){return $.attr(this,oldtitle);}).removeAttr(oldtitle);}
return elems;}}};$.each(PLUGINS.fn,function(name,func){if(!func||$.fn[name+replaceSuffix]){return TRUE;}
var old=$.fn[name+replaceSuffix]=$.fn[name];$.fn[name]=function(){return func.apply(this,arguments)||old.apply(this,arguments);};});if(!$.ui){$['cleanData'+replaceSuffix]=$.cleanData;$.cleanData=function(elems){for(var i=0,elem;(elem=elems[i])!==undefined;i++){try{$(elem).triggerHandler('removeqtip');}
catch(e){}}
$['cleanData'+replaceSuffix](elems);};}
QTIP.version='nightly';QTIP.nextid=0;QTIP.inactiveEvents='click dblclick mousedown mouseup mousemove mouseleave mouseenter'.split(' ');QTIP.zindex=15000;QTIP.defaults={prerender:FALSE,id:FALSE,overwrite:TRUE,suppress:TRUE,content:{text:TRUE,attr:'title',title:{text:FALSE,button:FALSE}},position:{my:'top left',at:'bottom right',target:FALSE,container:FALSE,viewport:FALSE,adjust:{x:0,y:0,mouse:TRUE,resize:TRUE,method:'flip flip'},effect:function(api,pos,viewport){$(this).animate(pos,{duration:200,queue:FALSE});}},show:{target:FALSE,event:'mouseenter',effect:TRUE,delay:90,solo:FALSE,ready:FALSE,autofocus:FALSE},hide:{target:FALSE,event:'mouseleave',effect:TRUE,delay:0,fixed:FALSE,inactive:FALSE,leave:'window',distance:FALSE},style:{classes:'',widget:FALSE,width:FALSE,height:FALSE,def:TRUE},events:{render:NULL,move:NULL,show:NULL,hide:NULL,toggle:NULL,visible:NULL,hidden:NULL,focus:NULL,blur:NULL}};function Ajax(api)
{var self=this,tooltip=api.elements.tooltip,opts=api.options.content.ajax,defaults=QTIP.defaults.content.ajax,namespace='.qtip-ajax',rscript=/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,first=TRUE,stop=FALSE,xhr;api.checks.ajax={'^content.ajax':function(obj,name,v){if(name==='ajax'){opts=v;}
if(name==='once'){self.init();}
else if(opts&&opts.url){self.load();}
else{tooltip.unbind(namespace);}}};$.extend(self,{init:function(){if(opts&&opts.url){tooltip.unbind(namespace)[opts.once?'one':'bind']('tooltipshow'+namespace,self.load);}
return self;},load:function(event){if(stop){stop=FALSE;return;}
var hasSelector=opts.url.indexOf(' '),url=opts.url,selector,hideFirst=!opts.loading&&first;if(hideFirst){try{event.preventDefault();}catch(e){}}
else if(event&&event.isDefaultPrevented()){return self;}
if(xhr&&xhr.abort){xhr.abort();}
if(hasSelector>-1){selector=url.substr(hasSelector);url=url.substr(0,hasSelector);}
function after(){var complete;if(api.destroyed){return;}
first=FALSE;if(hideFirst){stop=TRUE;api.show(event.originalEvent);}
if((complete=defaults.complete||opts.complete)&&$.isFunction(complete)){complete.apply(opts.context||api,arguments);}}
function successHandler(content,status,jqXHR){var success;if(api.destroyed){return;}
if(selector){content=$('<div/>').append(content.replace(rscript,"")).find(selector);}
if((success=defaults.success||opts.success)&&$.isFunction(success)){success.call(opts.context||api,content,status,jqXHR);}
else{api.set('content.text',content);}}
function errorHandler(xhr,status,error){if(api.destroyed||xhr.status===0){return;}
api.set('content.text',status+': '+error);}
xhr=$.ajax($.extend({error:defaults.error||errorHandler,context:api},opts,{url:url,success:successHandler,complete:after}));},destroy:function(){if(xhr&&xhr.abort){xhr.abort();}
api.destroyed=TRUE;}});self.init();}
PLUGINS.ajax=function(api)
{var self=api.plugins.ajax;return'object'===typeof self?self:(api.plugins.ajax=new Ajax(api));};PLUGINS.ajax.initialize='render';PLUGINS.ajax.sanitize=function(options)
{var content=options.content,opts;if(content&&'ajax'in content){opts=content.ajax;if(typeof opts!=='object'){opts=options.content.ajax={url:opts};}
if('boolean'!==typeof opts.once&&opts.once){opts.once=!!opts.once;}}};$.extend(TRUE,QTIP.defaults,{content:{ajax:{loading:TRUE,once:TRUE}}});PLUGINS.imagemap=function(area,corner,flip)
{if(!area.jquery){area=$(area);}
var shape=(area[0].shape||area.attr('shape')).toLowerCase(),baseCoords=(area[0].coords||area.attr('coords')).split(','),coords=[],image=$('img[usemap="#'+area.parent('map').attr('name')+'"]'),imageOffset=image.offset(),result={width:0,height:0,offset:{top:1e10,right:0,bottom:0,left:1e10}},i=0,next=0,dimensions;function polyCoordinates(result,coords,corner)
{var i=0,compareX=1,compareY=1,realX=0,realY=0,newWidth=result.width,newHeight=result.height;while(newWidth>0&&newHeight>0&&compareX>0&&compareY>0)
{newWidth=Math.floor(newWidth/2);newHeight=Math.floor(newHeight/2);if(corner.x==='left'){compareX=newWidth;}
else if(corner.x==='right'){compareX=result.width-newWidth;}
else{compareX+=Math.floor(newWidth/2);}
if(corner.y==='top'){compareY=newHeight;}
else if(corner.y==='bottom'){compareY=result.height-newHeight;}
else{compareY+=Math.floor(newHeight/2);}
i=coords.length;while(i--)
{if(coords.length<2){break;}
realX=coords[i][0]-result.offset.left;realY=coords[i][1]-result.offset.top;if((corner.x==='left'&&realX>=compareX)||(corner.x==='right'&&realX<=compareX)||(corner.x==='center'&&(realX<compareX||realX>(result.width-compareX)))||(corner.y==='top'&&realY>=compareY)||(corner.y==='bottom'&&realY<=compareY)||(corner.y==='center'&&(realY<compareY||realY>(result.height-compareY)))){coords.splice(i,1);}}}
return{left:coords[0][0],top:coords[0][1]};}
imageOffset.left+=Math.ceil((image.outerWidth()-image.width())/2);imageOffset.top+=Math.ceil((image.outerHeight()-image.height())/2);if(shape==='poly'){i=baseCoords.length;while(i--)
{next=[parseInt(baseCoords[--i],10),parseInt(baseCoords[i+1],10)];if(next[0]>result.offset.right){result.offset.right=next[0];}
if(next[0]<result.offset.left){result.offset.left=next[0];}
if(next[1]>result.offset.bottom){result.offset.bottom=next[1];}
if(next[1]<result.offset.top){result.offset.top=next[1];}
coords.push(next);}}
else{coords=$.map(baseCoords,function(coord){return parseInt(coord,10);});}
switch(shape)
{case'rect':result={width:Math.abs(coords[2]-coords[0]),height:Math.abs(coords[3]-coords[1]),offset:{left:Math.min(coords[0],coords[2]),top:Math.min(coords[1],coords[3])}};break;case'circle':result={width:coords[2]+2,height:coords[2]+2,offset:{left:coords[0],top:coords[1]}};break;case'poly':$.extend(result,{width:Math.abs(result.offset.right-result.offset.left),height:Math.abs(result.offset.bottom-result.offset.top)});if(corner.string()==='centercenter'){result.offset={left:result.offset.left+(result.width/2),top:result.offset.top+(result.height/2)};}
else{result.offset=polyCoordinates(result,coords.slice(),corner);if(flip&&(flip[0]==='flip'||flip[1]==='flip')){result.flipoffset=polyCoordinates(result,coords.slice(),{x:corner.x==='left'?'right':corner.x==='right'?'left':'center',y:corner.y==='top'?'bottom':corner.y==='bottom'?'top':'center'});result.flipoffset.left-=result.offset.left;result.flipoffset.top-=result.offset.top;}}
result.width=result.height=0;break;}
result.offset.left+=imageOffset.left;result.offset.top+=imageOffset.top;return result;};function calculateTip(corner,width,height)
{var width2=Math.ceil(width/2),height2=Math.ceil(height/2),tips={bottomright:[[0,0],[width,height],[width,0]],bottomleft:[[0,0],[width,0],[0,height]],topright:[[0,height],[width,0],[width,height]],topleft:[[0,0],[0,height],[width,height]],topcenter:[[0,height],[width2,0],[width,height]],bottomcenter:[[0,0],[width,0],[width2,height]],rightcenter:[[0,0],[width,height2],[0,height]],leftcenter:[[width,0],[width,height],[0,height2]]};tips.lefttop=tips.bottomright;tips.righttop=tips.bottomleft;tips.leftbottom=tips.topright;tips.rightbottom=tips.topleft;return tips[corner.string()];}
function Tip(qTip,command)
{var self=this,opts=qTip.options.style.tip,elems=qTip.elements,tooltip=elems.tooltip,cache={top:0,left:0},size={width:opts.width,height:opts.height},color={},border=opts.border||0,namespace='.qtip-tip',hasCanvas=!!($('<canvas />')[0]||{}).getContext;self.corner=NULL;self.mimic=NULL;self.border=border;self.offset=opts.offset;self.size=size;qTip.checks.tip={'^position.my|style.tip.(corner|mimic|border)$':function(){if(!self.init()){self.destroy();}
qTip.reposition();},'^style.tip.(height|width)$':function(){size={width:opts.width,height:opts.height};self.create();self.update();qTip.reposition();},'^content.title.text|style.(classes|widget)$':function(){if(elems.tip&&elems.tip.length){self.update();}}};function swapDimensions(){var temp=size.width;size.width=size.height;size.height=temp;}
function resetDimensions(){size.width=opts.width;size.height=opts.height;}
function reposition(event,api,pos,viewport){if(!elems.tip){return;}
var newCorner=self.corner.clone(),adjust=pos.adjusted,method=qTip.options.position.adjust.method.split(' '),horizontal=method[0],vertical=method[1]||method[0],shift={left:FALSE,top:FALSE,x:0,y:0},offset,css={},props;if(self.corner.fixed!==TRUE){if(horizontal==='shift'&&newCorner.precedance==='x'&&adjust.left&&newCorner.y!=='center'){newCorner.precedance=newCorner.precedance==='x'?'y':'x';}
else if(horizontal!=='shift'&&adjust.left){newCorner.x=newCorner.x==='center'?(adjust.left>0?'left':'right'):(newCorner.x==='left'?'right':'left');}
if(vertical==='shift'&&newCorner.precedance==='y'&&adjust.top&&newCorner.x!=='center'){newCorner.precedance=newCorner.precedance==='y'?'x':'y';}
else if(vertical!=='shift'&&adjust.top){newCorner.y=newCorner.y==='center'?(adjust.top>0?'top':'bottom'):(newCorner.y==='top'?'bottom':'top');}
if(newCorner.string()!==cache.corner.string()&&(cache.top!==adjust.top||cache.left!==adjust.left)){self.update(newCorner,FALSE);}}
offset=self.position(newCorner,adjust);offset[newCorner.x]+=borderWidth(newCorner,newCorner.x,TRUE);offset[newCorner.y]+=borderWidth(newCorner,newCorner.y,TRUE);if(offset.right!==undefined){offset.left=-offset.right;}
if(offset.bottom!==undefined){offset.top=-offset.bottom;}
offset.user=Math.max(0,opts.offset);if(shift.left=(horizontal==='shift'&&!!adjust.left)){if(newCorner.x==='center'){css['margin-left']=shift.x=offset['margin-left']-adjust.left;}
else{props=offset.right!==undefined?[adjust.left,-offset.left]:[-adjust.left,offset.left];if((shift.x=Math.max(props[0],props[1]))>props[0]){pos.left-=adjust.left;shift.left=FALSE;}
css[offset.right!==undefined?'right':'left']=shift.x;}}
if(shift.top=(vertical==='shift'&&!!adjust.top)){if(newCorner.y==='center'){css['margin-top']=shift.y=offset['margin-top']-adjust.top;}
else{props=offset.bottom!==undefined?[adjust.top,-offset.top]:[-adjust.top,offset.top];if((shift.y=Math.max(props[0],props[1]))>props[0]){pos.top-=adjust.top;shift.top=FALSE;}
css[offset.bottom!==undefined?'bottom':'top']=shift.y;}}
elems.tip.css(css).toggle(!((shift.x&&shift.y)||(newCorner.x==='center'&&shift.y)||(newCorner.y==='center'&&shift.x)));pos.left-=offset.left.charAt?offset.user:horizontal!=='shift'||shift.top||!shift.left&&!shift.top?offset.left:0;pos.top-=offset.top.charAt?offset.user:vertical!=='shift'||shift.left||!shift.left&&!shift.top?offset.top:0;cache.left=adjust.left;cache.top=adjust.top;cache.corner=newCorner.clone();}
function borderWidth(corner,side,backup){side=!side?corner[corner.precedance]:side;var isFluid=tooltip.hasClass(fluidClass),isTitleTop=elems.titlebar&&corner.y==='top',elem=isTitleTop?elems.titlebar:elems.tooltip,css='border-'+side+'-width',val;tooltip.addClass(fluidClass);val=parseInt(elem.css(css),10);val=(backup?val||parseInt(tooltip.css(css),10):val)||0;tooltip.toggleClass(fluidClass,isFluid);return val;}
function borderRadius(corner){var isTitleTop=elems.titlebar&&corner.y==='top',elem=isTitleTop?elems.titlebar:elems.content,moz=$.browser.mozilla,prefix=moz?'-moz-':$.browser.webkit?'-webkit-':'',side=corner.y+(moz?'':'-')+corner.x,css=prefix+(moz?'border-radius-'+side:'border-'+side+'-radius');return parseInt(elem.css(css),10)||parseInt(tooltip.css(css),10)||0;}
function calculateSize(corner){var y=corner.precedance==='y',width=size[y?'width':'height'],height=size[y?'height':'width'],isCenter=corner.string().indexOf('center')>-1,base=width*(isCenter?0.5:1),pow=Math.pow,round=Math.round,bigHyp,ratio,result,smallHyp=Math.sqrt(pow(base,2)+pow(height,2)),hyp=[(border/base)*smallHyp,(border/height)*smallHyp];hyp[2]=Math.sqrt(pow(hyp[0],2)-pow(border,2));hyp[3]=Math.sqrt(pow(hyp[1],2)-pow(border,2));bigHyp=smallHyp+hyp[2]+hyp[3]+(isCenter?0:hyp[0]);ratio=bigHyp/smallHyp;result=[round(ratio*height),round(ratio*width)];return{height:result[y?0:1],width:result[y?1:0]};}
$.extend(self,{init:function()
{var enabled=self.detectCorner()&&(hasCanvas||$.browser.msie);if(enabled){self.create();self.update();tooltip.unbind(namespace).bind('tooltipmove'+namespace,reposition);}
return enabled;},detectCorner:function()
{var corner=opts.corner,posOptions=qTip.options.position,at=posOptions.at,my=posOptions.my.string?posOptions.my.string():posOptions.my;if(corner===FALSE||(my===FALSE&&at===FALSE)){return FALSE;}
else{if(corner===TRUE){self.corner=new PLUGINS.Corner(my);}
else if(!corner.string){self.corner=new PLUGINS.Corner(corner);self.corner.fixed=TRUE;}}
cache.corner=new PLUGINS.Corner(self.corner.string());return self.corner.string()!=='centercenter';},detectColours:function(actual){var i,fill,border,tip=elems.tip.css('cssText',''),corner=actual||self.corner,precedance=corner[corner.precedance],borderSide='border-'+precedance+'-color',borderSideCamel='border'+precedance.charAt(0)+precedance.substr(1)+'Color',invalid=/rgba?\(0, 0, 0(, 0)?\)|transparent|#123456/i,backgroundColor='background-color',transparent='transparent',important=' !important',useTitle=elems.titlebar&&(corner.y==='top'||(corner.y==='center'&&tip.position().top+(size.height/2)+opts.offset<elems.titlebar.outerHeight(1))),colorElem=useTitle?elems.titlebar:elems.tooltip;tooltip.addClass(fluidClass);color.fill=fill=tip.css(backgroundColor);color.border=border=tip[0].style[borderSideCamel]||tip.css(borderSide)||tooltip.css(borderSide);if(!fill||invalid.test(fill)){color.fill=colorElem.css(backgroundColor)||transparent;if(invalid.test(color.fill)){color.fill=tooltip.css(backgroundColor)||fill;}}
if(!border||invalid.test(border)||border===$(document.body).css('color')){color.border=colorElem.css(borderSide)||transparent;if(invalid.test(color.border)||color.border===colorElem.css('color')){color.border=tooltip.css(borderSide)||tooltip.css(borderSideCamel)||border;}}
$('*',tip).add(tip).css('cssText',backgroundColor+':'+transparent+important+';border:0'+important+';');tooltip.removeClass(fluidClass);},create:function()
{var width=size.width,height=size.height,vml;if(elems.tip){elems.tip.remove();}
elems.tip=$('<div />',{'class':'ui-tooltip-tip'}).css({width:width,height:height}).prependTo(tooltip);if(hasCanvas){$('<canvas />').appendTo(elems.tip)[0].getContext('2d').save();}
else{vml='<vml:shape coordorigin="0,0" style="display:inline-block; position:absolute; behavior:url(#default#VML);"></vml:shape>';elems.tip.html(vml+vml);$('*',elems.tip).bind('click mousedown',function(event){event.stopPropagation();});}},update:function(corner,position)
{var tip=elems.tip,inner=tip.children(),width=size.width,height=size.height,regular='px solid ',transparent='px dashed transparent',mimic=opts.mimic,round=Math.round,precedance,context,coords,translate,newSize;if(!corner){corner=cache.corner||self.corner;}
if(mimic===FALSE){mimic=corner;}
else{mimic=new PLUGINS.Corner(mimic);mimic.precedance=corner.precedance;if(mimic.x==='inherit'){mimic.x=corner.x;}
else if(mimic.y==='inherit'){mimic.y=corner.y;}
else if(mimic.x===mimic.y){mimic[corner.precedance]=corner[corner.precedance];}}
precedance=mimic.precedance;if(corner.precedance==='x'){swapDimensions();}
else{resetDimensions();}
elems.tip.css({width:(width=size.width),height:(height=size.height)});self.detectColours(corner);if(color.border!=='transparent'){border=borderWidth(corner,NULL,TRUE);if(opts.border===0&&border>0){color.fill=color.border;}
self.border=border=opts.border!==TRUE?opts.border:border;}
else{self.border=border=0;}
coords=calculateTip(mimic,width,height);self.size=newSize=calculateSize(corner);tip.css(newSize);if(corner.precedance==='y'){translate=[round(mimic.x==='left'?border:mimic.x==='right'?newSize.width-width-border:(newSize.width-width)/2),round(mimic.y==='top'?newSize.height-height:0)];}
else{translate=[round(mimic.x==='left'?newSize.width-width:0),round(mimic.y==='top'?border:mimic.y==='bottom'?newSize.height-height-border:(newSize.height-height)/2)];}
if(hasCanvas){inner.attr(newSize);context=inner[0].getContext('2d');context.restore();context.save();context.clearRect(0,0,3000,3000);context.fillStyle=color.fill;context.strokeStyle=color.border;context.lineWidth=border*2;context.lineJoin='miter';context.miterLimit=100;context.translate(translate[0],translate[1]);context.beginPath();context.moveTo(coords[0][0],coords[0][1]);context.lineTo(coords[1][0],coords[1][1]);context.lineTo(coords[2][0],coords[2][1]);context.closePath();if(border){if(tooltip.css('background-clip')==='border-box'){context.strokeStyle=color.fill;context.stroke();}
context.strokeStyle=color.border;context.stroke();}
context.fill();}
else{coords='m'+coords[0][0]+','+coords[0][1]+' l'+coords[1][0]+','+coords[1][1]+' '+coords[2][0]+','+coords[2][1]+' xe';translate[2]=border&&/^(r|b)/i.test(corner.string())?parseFloat($.browser.version,10)===8?2:1:0;inner.css({antialias:''+(mimic.string().indexOf('center')>-1),left:translate[0]-(translate[2]*Number(precedance==='x')),top:translate[1]-(translate[2]*Number(precedance==='y')),width:width+border,height:height+border}).each(function(i){var $this=$(this);$this[$this.prop?'prop':'attr']({coordsize:(width+border)+' '+(height+border),path:coords,fillcolor:color.fill,filled:!!i,stroked:!i}).css({display:border||i?'block':'none'});if(!i&&$this.html()===''){$this.html('<vml:stroke weight="'+(border*2)+'px" color="'+color.border+'" miterlimit="1000" joinstyle="miter" '+' style="behavior:url(#default#VML); display:inline-block;" />');}});}
if(position!==FALSE){self.position(corner);}},position:function(corner)
{var tip=elems.tip,position={},userOffset=Math.max(0,opts.offset),precedance,dimensions,corners;if(opts.corner===FALSE||!tip){return FALSE;}
corner=corner||self.corner;precedance=corner.precedance;dimensions=calculateSize(corner);corners=[corner.x,corner.y];if(precedance==='x'){corners.reverse();}
$.each(corners,function(i,side){var b,br;if(side==='center'){b=precedance==='y'?'left':'top';position[b]='50%';position['margin-'+b]=-Math.round(dimensions[precedance==='y'?'width':'height']/2)+userOffset;}
else{b=borderWidth(corner,side,TRUE);br=borderRadius(corner);position[side]=i?border?borderWidth(corner,side):0:userOffset+(br>b?br:-b);}});position[corner[precedance]]-=dimensions[precedance==='x'?'width':'height'];tip.css({top:'',bottom:'',left:'',right:'',margin:''}).css(position);return position;},destroy:function()
{if(elems.tip){elems.tip.remove();}
elems.tip=false;tooltip.unbind(namespace);}});self.init();}
PLUGINS.tip=function(api)
{var self=api.plugins.tip;return'object'===typeof self?self:(api.plugins.tip=new Tip(api));};PLUGINS.tip.initialize='render';PLUGINS.tip.sanitize=function(options)
{var style=options.style,opts;if(style&&'tip'in style){opts=options.style.tip;if(typeof opts!=='object'){options.style.tip={corner:opts};}
if(!(/string|boolean/i).test(typeof opts.corner)){opts.corner=TRUE;}
if(typeof opts.width!=='number'){delete opts.width;}
if(typeof opts.height!=='number'){delete opts.height;}
if(typeof opts.border!=='number'&&opts.border!==TRUE){delete opts.border;}
if(typeof opts.offset!=='number'){delete opts.offset;}}};$.extend(TRUE,QTIP.defaults,{style:{tip:{corner:TRUE,mimic:FALSE,width:6,height:6,border:TRUE,offset:0}}});PLUGINS.svg=function(svg,corner)
{var doc=$(document),elem=svg[0],result={width:0,height:0,offset:{top:1e10,left:1e10}},box,mtx,root,point,tPoint;if(elem.getBBox&&elem.parentNode){box=elem.getBBox();mtx=elem.getScreenCTM();root=elem.farthestViewportElement||elem;if(!root.createSVGPoint){return result;}
point=root.createSVGPoint();point.x=box.x;point.y=box.y;tPoint=point.matrixTransform(mtx);result.offset.left=tPoint.x;result.offset.top=tPoint.y;point.x+=box.width;point.y+=box.height;tPoint=point.matrixTransform(mtx);result.width=tPoint.x-result.offset.left;result.height=tPoint.y-result.offset.top;result.offset.left+=doc.scrollLeft();result.offset.top+=doc.scrollTop();}
return result;};function Modal(api)
{var self=this,options=api.options.show.modal,elems=api.elements,tooltip=elems.tooltip,overlaySelector='#qtip-overlay',globalNamespace='.qtipmodal',namespace=globalNamespace+api.id,attr='is-modal-qtip',docBody=$(document.body),overlay;api.checks.modal={'^show.modal.(on|blur)$':function(){self.init();elems.overlay.toggle(tooltip.is(':visible'));}};$.extend(self,{init:function()
{if(!options.on){return self;}
overlay=self.create();tooltip.attr(attr,TRUE).css('z-index',PLUGINS.modal.zindex+$(selector+'['+attr+']').length).unbind(globalNamespace).unbind(namespace).bind('tooltipshow'+globalNamespace+' tooltiphide'+globalNamespace,function(event,api,duration){var oEvent=event.originalEvent;if(event.target===tooltip[0]){if(oEvent&&event.type==='tooltiphide'&&/mouse(leave|enter)/.test(oEvent.type)&&$(oEvent.relatedTarget).closest(overlay[0]).length){try{event.preventDefault();}catch(e){}}
else if(!oEvent||(oEvent&&!oEvent.solo)){self[event.type.replace('tooltip','')](event,duration);}}}).bind('tooltipfocus'+globalNamespace,function(event){if(event.isDefaultPrevented()||event.target!==tooltip[0]){return;}
var qtips=$(selector).filter('['+attr+']'),newIndex=PLUGINS.modal.zindex+qtips.length,curIndex=parseInt(tooltip[0].style.zIndex,10);overlay[0].style.zIndex=newIndex-1;qtips.each(function(){if(this.style.zIndex>curIndex){this.style.zIndex-=1;}});qtips.end().filter('.'+focusClass).qtip('blur',event.originalEvent);tooltip.addClass(focusClass)[0].style.zIndex=newIndex;try{event.preventDefault();}catch(e){}}).bind('tooltiphide'+globalNamespace,function(event){if(event.target===tooltip[0]){$('['+attr+']').filter(':visible').not(tooltip).last().qtip('focus',event);}});if(options.escape){$(window).unbind(namespace).bind('keydown'+namespace,function(event){if(event.keyCode===27&&tooltip.hasClass(focusClass)){api.hide(event);}});}
if(options.blur){elems.overlay.unbind(namespace).bind('click'+namespace,function(event){if(tooltip.hasClass(focusClass)){api.hide(event);}});}
return self;},create:function()
{var elem=$(overlaySelector);if(elem.length){return(elems.overlay=elem.insertAfter($(selector).last()));}
overlay=elems.overlay=$('<div />',{id:overlaySelector.substr(1),html:'<div></div>',mousedown:function(){return FALSE;}}).insertAfter($(selector).last());function resize(){overlay.css({height:$(window).height(),width:$(window).width()});}
$(window).unbind(globalNamespace).bind('resize'+globalNamespace,resize);resize();return overlay;},toggle:function(event,state,duration)
{if(event&&event.isDefaultPrevented()){return self;}
var effect=options.effect,type=state?'show':'hide',visible=overlay.is(':visible'),modals=$('['+attr+']').filter(':visible').not(tooltip),zindex;if(!overlay){overlay=self.create();}
if((overlay.is(':animated')&&visible===state)||(!state&&modals.length)){return self;}
if(state){overlay.css({left:0,top:0});overlay.toggleClass('blurs',options.blur);docBody.bind('focusin'+namespace,function(event){var target=$(event.target),container=target.closest('.qtip'),targetOnTop=container.length<1?FALSE:(parseInt(container[0].style.zIndex,10)>parseInt(tooltip[0].style.zIndex,10));if(!targetOnTop&&($(event.target).closest(selector)[0]!==tooltip[0])){tooltip.find('input:visible').filter(':first').focus();}});}
else{docBody.undelegate('*','focusin'+namespace);}
overlay.stop(TRUE,FALSE);if($.isFunction(effect)){effect.call(overlay,state);}
else if(effect===FALSE){overlay[type]();}
else{overlay.fadeTo(parseInt(duration,10)||90,state?1:0,function(){if(!state){$(this).hide();}});}
if(!state){overlay.queue(function(next){overlay.css({left:'',top:''});next();});}
return self;},show:function(event,duration){return self.toggle(event,TRUE,duration);},hide:function(event,duration){return self.toggle(event,FALSE,duration);},destroy:function()
{var delBlanket=overlay;if(delBlanket){delBlanket=$('['+attr+']').not(tooltip).length<1;if(delBlanket){elems.overlay.remove();$(window).unbind(globalNamespace);}
else{elems.overlay.unbind(globalNamespace+api.id);}
docBody.undelegate('*','focusin'+namespace);}
return tooltip.removeAttr(attr).unbind(globalNamespace);}});self.init();}
PLUGINS.modal=function(api){var self=api.plugins.modal;return'object'===typeof self?self:(api.plugins.modal=new Modal(api));};PLUGINS.modal.initialize='render';PLUGINS.modal.sanitize=function(opts){if(opts.show){if(typeof opts.show.modal!=='object'){opts.show.modal={on:!!opts.show.modal};}
else if(typeof opts.show.modal.on==='undefined'){opts.show.modal.on=TRUE;}}};PLUGINS.modal.zindex=QTIP.zindex+1000;$.extend(TRUE,QTIP.defaults,{show:{modal:{on:FALSE,effect:TRUE,blur:TRUE,escape:TRUE}}});function BGIFrame(api)
{var self=this,elems=api.elements,tooltip=elems.tooltip,namespace='.bgiframe-'+api.id;$.extend(self,{init:function()
{elems.bgiframe=$('<iframe class="ui-tooltip-bgiframe" frameborder="0" tabindex="-1" src="javascript:\'\';" '+' style="display:block; position:absolute; z-index:-1; filter:alpha(opacity=0); '+'-ms-filter:"progid:DXImageTransform.Microsoft.Alpha(Opacity=0)";"></iframe>');elems.bgiframe.appendTo(tooltip);tooltip.bind('tooltipmove'+namespace,self.adjust);},adjust:function()
{var dimensions=api.get('dimensions'),plugin=api.plugins.tip,tip=elems.tip,tipAdjust,offset;offset=parseInt(tooltip.css('border-left-width'),10)||0;offset={left:-offset,top:-offset};if(plugin&&tip){tipAdjust=(plugin.corner.precedance==='x')?['width','left']:['height','top'];offset[tipAdjust[1]]-=tip[tipAdjust[0]]();}
elems.bgiframe.css(offset).css(dimensions);},destroy:function()
{elems.bgiframe.remove();tooltip.unbind(namespace);}});self.init();}
PLUGINS.bgiframe=function(api)
{var browser=$.browser,self=api.plugins.bgiframe;if($('select, object').length<1||!(browser.msie&&(''+browser.version).charAt(0)==='6')){return FALSE;}
return'object'===typeof self?self:(api.plugins.bgiframe=new BGIFrame(api));};PLUGINS.bgiframe.initialize='render';}));(function($,document,window,console){"use strict";$(document).ready(function(){var tooltips=$('.oTooltip'),defaultPosition={viewport:$(".oPageContainer"),my:'top left',at:'bottom center'};tooltips.filter('.oTooltipHover[title]').qtip({position:defaultPosition,style:'ui-tooltip-rounded ui-tooltip-odesk'});tooltips.filter('.oTooltipPopup[data-tooltip]').qtip({position:defaultPosition,content:{attr:'data-tooltip',title:{text:function(){return $(this).attr('title');},button:true}},show:{event:'click'},style:'ui-tooltip-rounded ui-tooltip-odesk ui-tooltip-odesk-click',hide:false,events:{render:function(event,api){var e=event,x;x=e;$(window).bind('keydown',function(e){if(e.keyCode===27){api.hide(e);}});}}});tooltips.filter('.oTooltipPopup[title], .oTooltipPopup[oldtitle]').removeData("qtip").qtip({position:defaultPosition,style:'ui-tooltip-rounded ui-tooltip-odesk',content:{text:function(){return $(this).attr("title")||$(this).attr("oldtitle");}}});});}(jQuery,document,window,console));(function($){"use strict";$(document).ready(function(){var handler,initPage,html5historyEnabled=!!(history.pushState),loadUrl,timeoutRes=null;loadUrl=function(url,params){var timeout=null;if(url==="#"){return;}
timeout=window.setTimeout(function(){$('.oContractorSearch').addClass("oShaded");timeout=null;},2000);$.ajax(url,{type:'get',data:params,success:function(text,status,jqXhr){var realUrl=jqXhr.getResponseHeader('X-ODESK-PATH')||url,newContent=$(text),newBody=newContent.find(".oContractorSearch"),newBodyHtml=newBody.clone().wrap('<p>').parent().html();if(timeout){window.clearTimeout(timeout);}
history.pushState({content:newBodyHtml,path:realUrl},'',realUrl);$(".oContractorSearch").html("");$(".oContractorSearch").replaceWith(newBody);document.title=newContent.filter("title").text();$(document).trigger("o-refreshpage");},error:function(){$('.oContractorSearch').removeClass("oShaded");if(timeoutRes){window.clearTimeout(timeout);}}});};handler=function(){var form=$(this).closest("form");if(timeoutRes){window.clearTimeout(timeoutRes);}
timeoutRes=window.setTimeout(function(){timeoutRes=null;if(html5historyEnabled){loadUrl(window.location.href,form.serialize());}else{form.submit();}},10);};initPage=function(){$('.oRangeSelector select').bind("change",handler);$('.oRangeSelector select').bind("o-external-change",handler);$("li.checkbox input[type=checkbox]").not("[data-alturl]").bind("change",handler);if(html5historyEnabled){$('.oFacets a').bind("click",function(e){loadUrl($(this).attr("href"));e.preventDefault();return false;});$('.oPagination a').bind("click",function(e){loadUrl($(this).attr("href"));e.preventDefault();return false;});$('#search_form').bind("submit",function(){handler();return false;});}
$('.oHrsFacet .sCheckbox[data-alturl]').bind("click",function(){var url=$(this).data("alturl");if(html5historyEnabled){loadUrl(url);}else{window.location.href=url;}});};if(html5historyEnabled){window.addEventListener('popstate',function(e){if(e.state){$(".oContractorSearch").replaceWith(e.state.content);$(document).trigger("o-refreshpage");}},false);history.replaceState({content:$(".oContractorSearch").clone().wrap('<p>').parent().html()},document.title,document.location.href);}
initPage();$(document).bind("o-refreshpage",initPage);});}(jQuery));