YCW.namespace("Profiles.Widgets.vitality");

/**
 * Encapsulates all the functionality that a property needs to have on their (parent) page
 * and the functionality for the widget itself
 * <p>Usage: YCW.Profiles.Widgets.vitality.parentPage.init(configOptions)</p>
 * It requires ycw.yui.js and yui.cross-frame.js
 * @module YCW
 * @author Klaus Komenda <komenda@yahoo-inc.com>
 */

/**
 * Generic logging functionality within YCW. This is needed for using YUI Logger and needs
 * to check whether this is present on the page in order to not generate errors.
 * @class logger
 * @static
 */
YCW.Profiles.Widgets.vitality.logger = {
    log: function() {
        if (typeof YAHOO !== "undefined") {
            var m = arguments[0],
            t = "info";
            
            if (arguments[1]) {
                t = arguments[1];
            }
            YAHOO.log(m, t);
        }
    }
}

/**
 * All the functionality thats needed on the parent page, e.g. handling invite kiosk request,
 * resizing iframe (if applicable) etc.
 * @class parentPage
 * @static
 */
YCW.Profiles.Widgets.vitality.parentPage = function () {
    // shortcuts
    var log = YCW.Profiles.Widgets.vitality.logger.log;
    
    var config,
    urlParams,
    invitePanel,
    zIndex = 10;

    /**
     * Inserts Updates module (iframe) at the given location in the DOM
     * @method insertModule
     * @param {Element} obj DOM Element where the Updates modules should be put into
     */ 
    function insertModule(obj) {
        log("Inserting iframe");
        document.getElementById(config.containerId).appendChild(obj);
    }
    
    /**
     * Inserts Updates module markup into given container in DOM
     * @method insertMarkup
     * @param {String} mkup HTML markup returned by XHR 
     */ 
    function insertMarkup(mkup) {
        log("Inserting markup");
        YCW.util.Event.onAvailable(config.containerId, function () {
            document.getElementById(config.containerId).innerHTML = mkup;
        });
    }
    
    /**
     * URL encodes query string parameters
     * @method urlEncodeParams
     * @param {Object} p Parameters as object
     */ 
    function urlEncodeParams(p) {
        log("Encoding URL params");
        var params = "";
    
        for (var key in p) {
            if (p[key] !== '') {
                params = params + key + "=" + encodeURIComponent(p[key]) + "&";
            }
        }
        
        return params;
    }
    
    /**
     * Builds the modal mask for when the invite kiosk is invoked
     * @method buildMask
     * TODO: this should probably be refactored to use built-in YUI functionality somehow
     */ 
    function buildMask() {
        log("buildMask");
        
        var Dom = YCW.util.Dom;
        var maskEl = document.createElement("div");
        maskEl.id = "ypf-widget-mask";
        Dom.setStyle(maskEl, "opacity", 0.6);
        Dom.setStyle(maskEl, "filter", "alpha(opacity=60)");
        Dom.setStyle(maskEl, "background-color", "#000"); 
        Dom.setStyle(maskEl, "height", Dom.getDocumentHeight() + "px"); 
        Dom.setStyle(maskEl, "width", Dom.getViewportWidth() + "px"); 
        Dom.setStyle(maskEl, "left", 0); 
        Dom.setStyle(maskEl, "top", 0);
        Dom.setStyle(maskEl, "z-index", zIndex - 1); 
        Dom.setStyle(maskEl, "position", "absolute");
        
        document.body.insertBefore(maskEl, document.body.firstChild);
    }
    
    /**
     * Removes modal mask
     * @method removeMask
     */ 
    function removeMask() {
        log("Removing mask");
        document.body.removeChild(document.getElementById("ypf-widget-mask"));
    }
    
    /**
     * Removes invite kiosk iframe
     * @method removeFrame
     */ 
    function removeFrame() {
        log("Removing invite kiosk frame");
        document.body.removeChild(document.getElementById("invite-kiosk-frame"));
        removeMask();
    }
    
    /**
     * Loads invite kiosk iframe on the page
     * @method loadInviteKiosk
     */ 
    function loadInviteKiosk() {
        log("In function loadInviteKiosk");
        if (typeof YCW.widget.Overlay !== "undefined") {
            log("loadInviteKiosk");
            
            if (arguments.length > 0) {
                config = arguments[0];
            }
            
            if (!config.kioskUrl) {
                log("No kiosk URL passed in, using 'href' attribute on invite to connect link");
                try  { var pHost = YCW.util.Dom.get('ycw-invite-pHost').innerHTML; }
                  catch(e)  { var pHost = 'profiles.yahoo.com'; }
                config.kioskUrl = 'http://' + pHost + '/widgets/invitekiosk';
            }
            
            var iframe = document.createElement("iframe");
            iframe.src = config.kioskUrl + "?proxyUrl=" + encodeURIComponent(config.proxyUrl);

            if((config.invitee) && (config.invitee != null))  {
                iframe.src += '&invitee=' + encodeURIComponent(config.invitee);
                iframe.src += '&crumb=' + encodeURIComponent(config.crumb);
            }

            iframe.id                = "invite-kiosk-frame";
            iframe.scrolling         = "no";
            iframe.frameBorder       = "0";
            iframe.allowTransparency = "true";
            iframe.title             = "Invite Friends to Connect";
            YCW.util.Dom.setStyle(iframe, "overflow", "hidden");
            YCW.util.Dom.setStyle(iframe, "z-index", zIndex);
            
            document.body.appendChild(iframe);
            
            invitePanel = new YCW.widget.Overlay("invite-kiosk-frame", { 
                fixedcenter: true,
                visible:     false,
                width:       "470px",
                height:      "170px"
            });
            
            invitePanel.render(document.body);
            buildMask();
            invitePanel.show();
        
        } else {
            log("YCW.widget.Panel is undefined", "error");
        }
        
    }
    
	/**
	 * Handles incoming messages sent from iframe
	 * @method frameMsgHandler
	 * @param {String} type Event type, in this case: "onMessage"
	 * @param {Array} args Array of arguments that contains the message being passed in
	 * @param {Object} obj Object that can be passed in
	 **/
    function frameMsgHandler(type, args, obj) {
        var message = args[0],
        msgTokens,
        action;
        
        log("receiving message: " + message);

        // check if message sent concerns vitality
        if (typeof message === "string") {
            msgTokens = message.split("|");
            action = msgTokens[1];
            if (msgTokens[0] === "vitality") {
                switch (action) {
                case "adjustIframeHeight":
                    log("resizing iframe to " + msgTokens[2]);
                    YCW.util.Dom.setStyle("vitality-frame", "height", msgTokens[2]);
                    break;
                case "loadInviteKiosk":
                    // bring up panel using YUI container
                    // populate panel with iframe containing invite kiosk
                    config.kioskUrl = msgTokens[2];
                    if(msgTokens.length > 3)  {
                        config.invitee = msgTokens[3];
                        config.crumb = msgTokens[4];
                    }  else  {
                        config.invitee = null;
                        config.crumb = null;
                    }
                    loadInviteKiosk();
                    break;
                case "invitekiosk":
                    log("resizing invitekiosk iframe to " + msgTokens[2] + ", " + msgTokens[3]);
                    YCW.util.Dom.setStyle("invite-kiosk-frame", "width", msgTokens[2]);
                    YCW.util.Dom.setStyle("invite-kiosk-frame", "height", msgTokens[3]);
                    break;
                case "loadProfilesNux":
                    document.location = msgTokens[2];
                    break;
                default:
                    break;
                }
            } else if (msgTokens[0] === "invitekiosk") {
                switch (action) {
                case "adjustDim":
                    log("adjusting dimensions of invitekiosk iframe");
                    var vitaHeight = parseInt(msgTokens[3], '10');  // grab the numeric value of the proposed height
                    vitaHeight *= 2;  // double the height to handle longer names which might cause the overlay to grow vertically
                    vitaHeight += 'px';   // reappend 'px' to the end
                    YCW.util.Dom.setStyle("invite-kiosk-frame", "width", msgTokens[2]);
                    YCW.util.Dom.setStyle("invite-kiosk-frame", "height", vitaHeight);
                    // sequence to get the panel to fix itself
                    invitePanel.hide();
                    invitePanel.render();
                    invitePanel.show();
                    break;
                case "removeFrame":
                    log("removing invitekiosk iframe");
                    removeFrame();
                default:
                    break;
                }
            }
        }
    }
    
	/**
	 * Constructs the iframe URL based on config params provided,
	 * creates iframe element with various params and adds it
	 * to the DOM
	 * @method loadFrame
	 **/
    function loadFrame() {
        log("load Vitality Frame");
        urlParams = {};
        
        if (config.params !== "undefined") {
            urlParams = config.params;
        }
        
        if (config[".crumb"]) {
            urlParams[".crumb"] = config[".crumb"];
        } else {
            log("No crumb defined", "error");
        }
        
        if (config.proxyUrl) {
            urlParams.proxyUrl = config.proxyUrl;
        } else {
            log("No proxyUrl defined", "error");
        }

        urlParams = urlEncodeParams(urlParams);
        
        // put iframe on page
        var iframeEl = document.createElement("iframe");
        iframeEl.src = config.ycwHost + config.ycwEndpoint + "?format=html&" + urlParams;
        
        var loc = parent.document.location.toString();
        
        if (config[".done"]) {
            log("appending .done");
            iframeEl.src += ".done=" + encodeURIComponent(config[".done"]);
        }

        if (loc.indexOf("apidebug=1") !== -1) {
            log("appending 'apidebug=1'");
            iframeEl.src += "&apidebug=1";
        }
        
        iframeEl.id                = "vitality-frame";
        iframeEl.scrolling         = "no";
        iframeEl.frameBorder       = "0";
        iframeEl.allowTransparency = "true";
        iframeEl.title             = "Yahoo! Updates";
        YCW.util.Dom.setStyle(iframeEl, "overflow", "hidden");
    
        if (typeof config.params.module_width_px !== "undefined") {
            log("Setting initial width on iframe");
            YCW.util.Dom.setStyle(iframeEl, "width", config.params.module_width_px + "px");
        }
        
        YCW.util.Event.onAvailable(config.containerId, insertModule, iframeEl);
    }
    
	/**
     * Executes when the script node was successfully put into the HEAD of the document
	 * @method successHandler
	 * @param {Object} o Get Utility Script Node object
	 **/
    function successHandler(o) {
        log("Script request was successfull");
        o.purge(); //removes the script node immediately after executing
        YCW.Profiles.Widgets.vitality.widget.init(config);
    }
    
	/**
     * Executes when there was an error when using GET utility to get Markup
	 * @method failureHandler
	 **/
    function failureHandler() {
        log("Getting markup using script element failed", "error");
    }
   
	/**
     * Main init function
	 * @method init
	 * @param {Object} c YCW configuration object
	 **/
    function init(c) {
        var param,ycwHost,ycwEndpoint,delim,idx,qIdx;
        log("parentPage.init");
        config = c;

        if (config.ycwHost === undefined) {
            url = config.baseUrl;
            delim = url.indexOf('/');
            idx = url.indexOf('/',delim + 2);

            // strip of everything after the question mark
            qIdx = url.indexOf('?');
            if(qIdx < 0)  { qIdx = url.length; }

            // substr the snot out of the url to make the new ycw params
            config.ycwHost = url.substr(0,idx);
            config.ycwEndpoint = url.substr(idx,(qIdx-idx));
        } else {
            // remove trailing slash
            hostLen = config.ycwHost.length;
            if (config.ycwHost.charAt(hostLen - 1) == '/') {
                config.ycwHost = config.ycwHost.substr(0, hostLen - 1);
            }
        }
        
        // subscribe to frameMsg Handler
        // this is used in any case, because the invite kiosk iframe might
        // need to communicate with the parent
        YCW.util.CrossFrame.onMessageEvent.subscribe(frameMsgHandler);
        
        if (config.type === "iframe") {
            log("iframe mode");
            // put iframe on the page
            loadFrame(); 
        } else if (config.type === "embed") {
            log("embed mode");
            // request markup from baseURL
            urlParams = {};
            if (config.params !== "undefined") {
                urlParams = config.params;
            }
            
            if (config[".crumb"]) {
                urlParams[".crumb"] = config[".crumb"];
                urlParams = urlEncodeParams(urlParams);
            } else {
                log("No crumb was passed in", "error");
            }

            if(config[".done"]) {
                param = {};
                param[".done"] = config[".done"]; 
                urlParams += urlEncodeParams(param);
            }

            // use Get utility to get markup data
            log("Using Get utility to get markup");
            YCW.util.Get.script(config.ycwHost + config.ycwEndpoint + "?format=html&" + urlParams + "&callback=YCW.Profiles.Widgets.vitality.parentPage.insertMarkup", { onSuccess: successHandler, onFailure: failureHandler });
        } else if (config.type === "curl") {
            log("curl mode");
            YCW.Profiles.Widgets.vitality.widget.init(config);
        }
    }
    
    return {
        init: init,
        insertMarkup: insertMarkup,
        loadFrame: loadFrame,
        loadInviteKiosk: loadInviteKiosk,
        getMarkupSuccessHandler: successHandler,
        getMarkupFailureHandler: failureHandler,
        frameMsgHandler: frameMsgHandler
    };
}();


YCW.Profiles.Widgets.vitality.widget = function () {
    // shortcuts
    var YUD = YCW.util.Dom,
    YUE = YCW.util.Event,
    YUCF = YCW.util.CrossFrame
    log = YCW.Profiles.Widgets.vitality.logger.log;

    var vitaContainerEl,
    proxyUrl,
    itemActionsEl,
    timer = {},
    itemRegions = [],
    eventItemEls,
    currentEventItemEl,
    itemActionsOverlay,
    inviteToolTipOverlay,
    config,
    mousePos,
    dropDownRegions = [],
    overlayShown = false,
    pHost,
    inviteKioskClickTimestamp;
        
    /**
     * Returns height (offsetHeight) of given Element in DOM
     * @method getHeight
     * @param {Element} el DOM Element to calculate height of
     * @returns {Integer} offsetHeight of given Element
     */ 
    function getHeight(el) {
        log("getHeight");
        return el.offsetHeight;
    }
        
    /**
     * Sends calculated height of container to parent frame using CrossFrame
     * @method sendHeightToParent
     */ 
    function sendHeightToParent() {
        log("sendHeightToParent");
        YUCF.send(proxyUrl, "parent", "vitality|adjustIframeHeight|" + getHeight(vitaContainerEl) + "px");
    }
        
    /**
     * Hides the drop-down/fly-in menu from display
     * @method hideOverlay
     */  
    function hideOverlay() {
        log("hideOverlay called");

        YUD.addClass(itemActionsEl, "ycw-no-display");
        itemActionsOverlay.hide();
        overlayShown = false;
    }
    
    /**
     * Processes the response coming back from socDir and either redirects to
     * Profiles or brings up Invite Kiosk, depending on NUX state of user
     * @method processSocDirResponse
     * @param {Object} socDirData Response from Social Directory
     */ 
    function processSocDirResponse(socDirData) {
        log("processing socdir response");

        // if redirect url
        if (socDirData.redirect) {
            log("redirect to:" + socDirData.redirect);
            if (config.type === "iframe" || typeof config.type === "undefined") {
                log("send redirect message to iframe");
                // send message to parent
                log("Sending msg to parent to open invite kiosk");
                YUCF.send(proxyUrl, "parent", "vitality|loadProfilesNux|" + socDirData.redirect);
            } else {
                log("redirect from current page");
                // make invite kiosk url point to profiles
                // purge element first
                //YUE.purgeElement(YUD.get("ycw-invite-to-connect"), "click", true);
                document.location = socDirData.redirect; 
            }

        } else {
            // based on mode, either send command to parent to bring up
            // invite kiosk or create it right on the current page.
            if (config.type === "iframe" || typeof config.type === "undefined") {
                // send message to parent
                log("Sending msg to parent to open invite kiosk");
                var kioskURL = 'http://' + pHost + '/widgets/invitekiosk';
                if((config.invitee) && (config.invitee != null))  {
                    YUCF.send(proxyUrl, "parent", "vitality|loadInviteKiosk|" + kioskURL + "|" + config.invitee + "|" + config.crumb);
                }  else  {
                    YUCF.send(proxyUrl, "parent", "vitality|loadInviteKiosk|" + kioskURL);
                }
                
            } else {
                // load invite kiosk on same page
                log("Loading kiosk on same page");
                YCW.Profiles.Widgets.vitality.parentPage.loadInviteKiosk(config);
            }
        }
    }
    
    /**
     * Handles the mouseover action when someone moves into a region 
     * for an update where the author and viewer are not connected.
     * This was added in the 1.7 release to support hotstart
     * @name inviteToolTip
     * @param (HTMLelement) eventEl  markup element of the event
     * @author mich cook <mich@yahoo-inc.com>
     */
    function inviteToolTip(eventEl)  {
        // get the nickname of the event author
        var newNickname = YUD.getElementsByClassName("nickname", "a", eventEl);
        if(newNickname.length != 1)  { newNicknameText = 'this user'; }
        else  { newNicknameText = newNickname[0].innerHTML; }

        // insert the nickname into the tooltip
        var tooltipNames = YUD.getElementsByClassName('profile-nickname', 'span', 'ycw-invite-tool-tip');
        for(var tnCount = 0; tnCount < tooltipNames.length; tnCount++)  {
            tooltipNames[tnCount].innerHTML = newNicknameText + ' '; // we add a space to try to overcome some of the complete state of broken that is ie6
        }

        // truncate the nickname for the link since we can have much much less space to use for it
        if(newNicknameText.length > 10)  {
            newNicknameText = newNicknameText.substring(0,7) + '...';
        }
        
        // insert the nickname into the invite link
        var profileNames = YUD.getElementsByClassName("profile-nickname", "span", eventEl);
        for(var pnCount = 0; pnCount < profileNames.length; pnCount++)  {
            profileNames[pnCount].innerHTML = newNicknameText + ' ';  // we add a space to try to overcome some of the complete state of broken that is ie6
        }

        // hide the time
        YUD.addClass(eventEl, "hover-hide");

        // show the tool tip to really get them to click
        inviteToolTipOverlay.cfg.setProperty("context", [eventEl, 'tr', 'br']);
        YUE.addListener(profileNames[0].parentNode, 'mouseover', function() { YUD.removeClass('ycw-invite-tool-tip', 'ycw-no-display'); inviteToolTipOverlay.show(); });
        YUE.addListener(profileNames[0].parentNode, 'mouseout', function() { YUD.addClass('ycw-invite-tool-tip', 'ycw-no-display'); inviteToolTipOverlay.hide(); });

        storeRegions();
    }

    /**
     * Goes through all the events and finds the ones that match the guid
     * These events are then replaced with a message that they were deleted
     * This was added in the 1.7 release to support hotstart
     * @name replaceIgnoredEvents
     * @param {string} targetGuid  The guid of the author that is being ignored
     * @author mich cook <mich@yahoo-inc.com>
     */
    function replaceIgnoredEvents(targetGuid)  {

        // get the nickname of the ignored author
        var newNickname = YUD.getElementsByClassName("nickname", "a", currentEventItemEl);
        if(newNickname.length != 1)  { newNicknameText = 'this user'; }
          else  { newNicknameText = newNickname[0].innerHTML; }

        // insert the nickname into the text
        var ycwIgnore = YUD.get('ycw-ignore-text');
        var replaceNicknames = YUD.getElementsByClassName('profile-nickname', 'span', ycwIgnore);
        for(var i = 0; i < replaceNicknames.length; i++)  {
            replaceNicknames[i].innerHTML = newNicknameText + ' ';
        }

        // get a list of all the events
        var allEvents = YUD.getElementsByClassName('ycw-report-abuse', 'a', 'ycw-event-list');

        // iterate over all the events in the list
        for(var eventCount = 0; eventCount < allEvents.length; eventCount++)  {

            // find the author guid of the event
            authorGuid = getQueryStringParameter('guid', allEvents[eventCount].href);

            // replace the contents of the event if the author is being ignored
            if(authorGuid == targetGuid)  {
                eventNode = allEvents[eventCount];

                // get the LI element
                eventNode = allEvents[eventCount].parentNode;

                // replace the event with the text saying it was replaced
                eventNode.innerHTML = ycwIgnore.innerHTML; // 'i have been replaced';

                // remove classes that are no longer relevant
                YUD.removeClass(eventNode, 'ycwPhoto');
                YUD.removeClass(eventNode, 'ycw-no-user-photo');

                // add class for item that has been replaced and ignored
                YUD.addClass(eventNode, 'ycw-ignore-replaced-item');
            }
        }

        // recreate all the mouseover regions since the contents have been replaced
        // by replacing the contents, we have most likely shifted the regions
        storeRegions();
    }


    /**
     * Handles the ignore request when someone clicks on the link
     * This was added in the 1.7 release to support hotstart
     * @name doIgnoreRequest
     * @param (event object) event object passed in by the listener
     * @author mich cook <mich@yahoo-inc.com>
     */
    function doIgnoreRequest(e)  {

        YUE.preventDefault(e);

        var reportAbuseUrl = YUD.getElementsByClassName('ycw-report-abuse', 'a', currentEventItemEl);
        var crumb = YUD.getElementsByClassName('ycw-ignore-crumb', 'p', currentEventItemEl);
        crumb = crumb[0].innerHTML;
        var myGuid = YUD.getElementsByClassName('ycw-guid', 'p', currentEventItemEl);
        myGuid = myGuid[0].innerHTML;
        var targetGuid = getQueryStringParameter('guid', reportAbuseUrl[0].href);

        scripturl = config.ycwHost + '/v1/user/' + myGuid + '/ignoreSuggestions?.crumb=' + crumb + '&guid=' + targetGuid;

        var processIgnoreEventResponse =  function (o) {
            replaceIgnoredEvents(targetGuid);
        };

        var processIgnoreEventFailure = function (o) {
            log("Ignore fails with status = " + o.status);
        };

        YCW.util.Get.script(scripturl,{ onSuccess: processIgnoreEventResponse, onFailure: processIgnoreEventFailure});
    }

    /**
     * Handles mouse movement on the document and, if the mouse enters/leaves
     * an event item in the list, set/removes the "hover" class from the element
     * If the dropDown Menu is present, it also checks whether the mouse cursor
     * is inside the Menu area and initiates a timer if the cursor leaves the area
     * @method mouseMoveHandler
     * @param {Event} e Event Object being passed in
     */
    function mouseMoveHandler(e) {
        var coordsXY = YUE.getXY(e);
        mousePos = new YCW.util.Point(coordsXY[0], coordsXY[1]);
        
        for (var i = 0, len = eventItemEls.length; i < len; i++) {
            if (itemRegions[i].contains(mousePos)) {
                if (!YUD.hasClass(eventItemEls[i], "hover")) {
                    YUD.addClass(eventItemEls[i], "hover");
                }
                // if there's a link for inviting someone, show it
                if(YUD.getElementsByClassName('ycw-invite-link', 'a', eventItemEls[i]).length != 0)  {
                    inviteToolTip(eventItemEls[i]);
                }
            } else {
                if (YUD.hasClass(eventItemEls[i], "hover")) {
                    YUD.removeClass(eventItemEls[i], "hover");
                    YUD.removeClass(eventItemEls[i], "hover-hide");
                }
            }
        }
        
        // if dropDown Menu is shown at the moment
        if (overlayShown) {
            
            // cancel any running timer, if applicable
            if (timer.cancel) {
                timer.cancel();
            }
            
            // check if mouse curser is either over dropdown icon or report abuse link area
            if (!dropDownRegions[0].contains(mousePos) && !dropDownRegions[1].contains(mousePos)) {
                // if not, initiate timer to hide overlay
                timer = YCW.lang.later(400, null, hideOverlay, null, false);
            }   
        }
    }
   
    /**
     * Finds the high of the text of an element
     * Can be/is used to try to determine if text has wrapped in an element
     * This was added in the 1.7 release to support hotstart
     * @name getTextHeight
     * @param (HTMLelement) markupEl  markup element of the text
     * @author mich cook <mich@yahoo-inc.com>
     */
    function getTextHeight(markupEl)  {

        // get the padding values
        var toppadding = YUD.getStyle(markupEl, 'paddingTop');
        var bottompadding = YUD.getStyle(markupEl, 'paddingBottom');

        // strip off the 'px'
        var pxStripPattern = /[px]/gi;
        bottompadding = bottompadding.replace(pxStripPattern, '');
        toppadding = toppadding.replace(pxStripPattern, '');

        // send back the result
        return(markupEl.offsetHeight - toppadding - bottompadding);
    }

    /**
     * Finds out the right positioning for the action item drop-down/fly-in
     * menu and displays it
     * @method showActionDropDown
     * @param {Event} e Event Object being passed in
     */
    function showActionDropDown(e) {
        log("showActionDropDown clicked");
        
        var parent,
        currentTop,
        coordsXY;
        
        // cancel the timer if it is running, cause no need to hide panel now
        if (timer.cancel) {
            timer.cancel();
        }
        
        // loop up starting from the target until list item node is reached
        parent = YUE.getTarget(e).parentNode;
        while (parent.nodeName !== "LI") {
            parent = parent.parentNode;
        }
        
        currentEventItemEl = parent;
        
        YUD.removeClass(itemActionsEl, "ycw-no-display");
        
        // from there, get the report abuse URL to populate the href in the
        // report abuse link in the dropdown
        var abuseUrl = YUD.getElementsByClassName("ycw-report-abuse", "a", parent)[0].href;
        YUD.get("ycw-report-abuse-cta").href = abuseUrl;
        
        // insert the nickname into the strings
        var newNickname = YUD.getElementsByClassName("nickname", "a", currentEventItemEl);
        if(newNickname.length != 1)  { newNicknameText = 'this user'; }
        else  { newNicknameText = newNickname[0].innerHTML; }

        var profileNames = YUD.getElementsByClassName("profile-nickname", "span", "ycw-action-item-bar");
        for(var pnCount = 0; pnCount < profileNames.length; pnCount++)  {
            profileNames[pnCount].innerHTML = newNicknameText + ' ';
        }

        // had to upgrade to yui 2.7.0 to make repositioning on resizing work
        itemActionsOverlay.cfg.setProperty("context", [YUE.getTarget(e), "tr", "tr", ["beforeShow", "windowResize"]]);

        // show/hide the invite link accordingly
        if(YUD.getElementsByClassName('ycw-invite-link', 'a', currentEventItemEl).length != 0)  {
            displayStyle = 'block';
            inviteLinks = YUD.getElementsByClassName('ycw-invite-link', 'a', currentEventItemEl);
            YUD.get('ycw-invite-x-cta').href = inviteLinks[0].href;
        }  else  {
            displayStyle = 'none';
        }
        YUD.setStyle('ycw-invite-x-cta', 'display', displayStyle);

        // show/hide the ignore link accordingly
        if(YUD.getElementsByClassName('ycw-ignore-link', 'a', currentEventItemEl).length != 0)  {
            displayStyle = 'block';
            inviteLinks = YUD.getElementsByClassName('ycw-ignore-link', 'a', currentEventItemEl);
            YUD.get('ycw-ignore-x-cta').href = inviteLinks[0].href;
        }  else  {
            displayStyle = 'none';
        }
        YUD.setStyle('ycw-ignore-x-cta', 'display', displayStyle);

        // reset the width of the overlay to the original
        olWidth = 150;
        itemActionsOverlay.cfg.setProperty("width", olWidth + 'px');

        // init the vars for the loop below
        var inviteHeight = getTextHeight(YUD.get('ycw-invite-x-cta'));
        var ignoreHeight = getTextHeight(YUD.get('ycw-ignore-x-cta'));
        var abuseHeight  = getTextHeight(YUD.get('ycw-report-abuse-cta'));

        // as long as the difference between the heights is more than half the height of the abuse text, we make it wider
        // at first, i tried just comparing the text heights directly, but was getting weird values back intermittenntly
        // these values were only larger by a few pixels, so they shouldn't ever be more than half
        // since we can't really tell how much longer the text is, we just keep adding a bit to the width until it stops being too high
        // subtle flaw: having this work as intended completely depends on the abuse link not wrapping. 
        while((((inviteHeight - abuseHeight) > (abuseHeight*0.5)) || ((ignoreHeight - abuseHeight) > (abuseHeight*0.5))) && (olWidth < 650)) {

            // get the number value of the current width and add 10 to it
            olWidth+= 10;

            // set the new width on the overlay
            itemActionsOverlay.cfg.setProperty("width", olWidth + 'px');

            // update our vars for the next iteration of the loop
            inviteHeight = getTextHeight(YUD.get('ycw-invite-x-cta')); // update invite height
            ignoreHeight = getTextHeight(YUD.get('ycw-ignore-x-cta')); // update ignore height
        }

        // funny little sequence that seems to get it drawn correctly        
        itemActionsOverlay.render();
        itemActionsOverlay.hide();
        itemActionsOverlay.show();
               
        // correct absolute positioning slightly to place icons exactly on top of each other
        currentTop = parseInt(YUD.getStyle(YUD.get("ycw-item-actions_c"), "top"), 10);
        YUD.setStyle(YUD.get("ycw-item-actions_c"), "top",  (currentTop - 3) + "px");
        
        overlayShown = true;
        
        // get XY for "ycw-button-icn"
        dropDownRegions[0] = YUD.getRegion("ycw-button-icn");
        
        // get XY for "ycw-action-item-bar"
        dropDownRegions[1] = YUD.getRegion("ycw-action-item-bar");
        
        // as soon as user moves out of overlay, initialize timer to close overlay
        YUE.addListener("ycw-item-actions_c", "mouseover", function (e) {
            //var mouseoverTarget = YUE.getTarget(e);
            //var actionItemIds = [
            //    "ycw-item-actions_c",
            //    "ycw-item-actions",
            //    "ycw-button-icn",
            //    "ycw-action-item-bar",
            //    "ycw-report-abuse-cta"
            //];
            //
            //for (var i = 0, len = actionItemIds.length; i < len; i++) {
            //    if (mouseoverTarget.id === actionItemIds[i]) {
            //        if (timer.cancel) {
            //            console.log("cancelling timer on mouseover");
            //            timer.cancel();
            //        }
            //    }
            //}
        });
    }
    
    /**
     * Executed when Social Directory WS request was successful
     * @method socDirSuccessHandler
     */
    function socDirSuccessHandler(o) {
        log("call to invitekiosk ws was successful");
        processSocDirResponse(ycwInviteKioskResponse);
    }
    
    /**
     * Executed when Social Directory WS request failed
     * @method socDirFailureHandler
     */
    function socDirFailureHandler() {
        log("call to invitekiosk ws failed", "error");
    }
    
    /**
     * Handles the click on the 'invite to connect' link, checks whether
     * the user did not click on that link twice within a reasonable amount of time
     * passed and makes request to SocDir to get NUX state for the user.
     * This was added in the YCW 1.7 release to support hotstart, which allows you to invite people in the list
     * @method inviteKioskClickHandler
     * @param {Event} e Event Object being passed in
     */
    function inviteKioskClickHandler(e) {
        log("Initiating Invite Kiosk");
        var eventItem = null;

        YUE.preventDefault(e);
        
        var targetEl = YUE.getTarget(e);  // get the link that was clicked

        // if it's the span in the middle of a link move up a node
        if(YUD.hasClass(targetEl, 'profile-nickname'))  { targetEl = targetEl.parentNode; }

        // ycw-invite-x-cta is the invite link in the dropdown
        if(targetEl.id == 'ycw-invite-x-cta')  {
            eventItem = currentEventItemEl;
        }

        // ycw-invite-link are the hover links that appear asking the user to connect
        if(YUD.hasClass(targetEl, 'ycw-invite-link'))  {
            eventItem = targetEl.parentNode;
            while (eventItem.nodeName !== "LI") {
                eventItem = eventItem.parentNode;
            }
        }

        // we have an eventItem if one of the clicked links matches the logic above
        if(eventItem != null)  {
            // get crumb and tGuid
            var inviteCrumb = YUD.get('ycw-invite-crumb').innerHTML;
            var tGuids = YUD.getElementsByClassName('ycw-target-guid', 'p', eventItem);

            // there's only one of these per item, so it'll always be index 0. 
            config.invitee = tGuids[0].innerHTML;
            config.crumb = inviteCrumb;
        }  else  {
            // if we don't have an eventItem, clear out the settings so they don't linger
            config.invitee = null;
            config.crumb = null;
        }

        var showKiosk = false;
        
        if (!inviteKioskClickTimestamp) {
            // invite kiosk link was clicked the first time
            // store timestamp
            inviteKioskClickTimestamp = new Date().getTime();
            showKiosk = true;
            
        } else {
            // check if clicks on link were x seconds apart
            var now = new Date().getTime();
            if ((now - inviteKioskClickTimestamp) >= 2000) {
                showKiosk = true;
                inviteKioskClickTimestamp = now;
            } else {
                log("Not enough time passed between click on links");
            }
        }
        
        if (showKiosk) {
            var getURL = 'http://' + pHost + '/ws/invitekiosk?responsetype=assignment';
            YCW.util.Get.script(getURL, { onSuccess: socDirSuccessHandler, failure: socDirFailureHandler, win: window, insertBefore: 'ycw-container' });
        }
    }
    
    /**
     * Handles clicks on either the "Everyone" or the "My Connections" tab
     * @method tabClickHandler
     * @param {Event} e Event Object being passed in
     */
    function tabClickHandler(e) {
        log("Tab was clicked");

        YUE.preventDefault(e);
        
        if (YUE.getTarget(e).href) {
            
            var linksrc = YUE.getTarget(e).href;
            
            if (linksrc.indexOf("callback") === -1) {
                log("add callback param to URL");
                linksrc += "&callback=YCW.Profiles.Widgets.vitality.parentPage.insertMarkup";
            }
            
            if (linksrc.indexOf("crumb") === -1) {
                log("add crumb to URL");
                linksrc += "&.crumb=" + config[".crumb"];
            }
            
            YCW.util.Get.script(linksrc , {
            onSuccess: YCW.Profiles.Widgets.vitality.parentPage.getMarkupSuccessHandler,
                           onFailure: YCW.Profiles.Widgets.vitality.parentPage.getMarkupFailureHandler
                           });
        }
    }
    
    /**
     * Contains event listener set up on load of the page
     * @method attachEventListeners
     */
    function attachEventListeners() {
        log("Attaching Event Listeners");

        // mouse move listener to check which item the user is hovering over
        YUE.addListener(document, "mousemove", mouseMoveHandler);
        
        // show interactive drop down menu
        YUE.addListener(YUD.get("ycw-event-list"), "click", function (e) {
            var target = YUE.getTarget(e);
            var targetNodeName = target.nodeName.toUpperCase();
            
            if (targetNodeName === "BUTTON" ||
                (targetNodeName === "IMG" && target.parentNode.nodeName.toUpperCase() === "BUTTON")) {
                showActionDropDown(e);
            }
        });
        
        // invite kiosk
        log("attach event listener for 'invite to connect' link");
        var inviteLinks = YUD.getElementsByClassName('ycw-invite-link', 'a', 'ycw-container');
        if(inviteLinks.length > 0)  {
            YUE.addListener(inviteLinks, "click", inviteKioskClickHandler);
        }

        if (YUD.get("ycw-invite-x-cta")) {
            YUE.addListener('ycw-invite-x-cta', "click", inviteKioskClickHandler);
        }

        if (YUD.get("ycw-invite-to-connect")) {
            YUE.addListener(YUD.get("ycw-invite-to-connect"), "click", inviteKioskClickHandler);
        }

        // ignore link
        YUE.addListener('ycw-ignore-x-cta', 'click', doIgnoreRequest);
        
        // tab switching
        // get anchors
        if (config.type === "embed" || config.type === "curl") {
            if (YUD.get("ycw-tabs")) {
                log("attaching event listener to tabs");
                var tabContainerEl = document.getElementById("ycw-tabs");
                var tabAnchors = tabContainerEl.getElementsByTagName("a");
                YUE.addListener(tabAnchors, "click", tabClickHandler);
            }
        }

        // delete event
        log('attaching event listener to delete event links');
        YUE.addListener(YUD.getElementsByClassName('ycw-delete-event'),'click', deleteEventClickHandler);
    }
    
    /**
     * Populating .done parameter in sign in/sign up or manage my updates link
     * @method populateDone
     */
    function populateDone() {
        log("Populating .done parameter");
        var done;
        
        // get source for .done
        if (config.type === "iframe" || typeof config.type === "undefined") {
            done = "http://www.yahoo.com/";
        } else {
            done = document.location;
        }

        // check if sign up/sign in links are there
        if (YUD.get("ycw-sign-in") && YUD.get("ycw-sign-up")) {
            // populate .done, if it is not alredy there
            if (YUD.get("ycw-sign-in").href.indexOf(".done") === -1) {
                // sign in
                log("populate .done in signIN link");
                YUD.get("ycw-sign-in").href += "&.done=" + encodeURIComponent(done);
            }
            
            if (YUD.get("ycw-sign-up").href.indexOf(".done") === -1) {
                // sign up
                log("populate .done in signUP link");
                YUD.get("ycw-sign-up").href += "&.done=" + encodeURIComponent(done);
            }
        }
        
        if (YUD.get("ycw-mng-upd")) {
            if (YUD.get("ycw-mng-upd").href.indexOf(".done") === -1) {
                // manage my updates
                log("populate .done in 'manage my updates' link");
                YUD.get("ycw-mng-upd").href += "&.done=" + encodeURIComponent(done);
            }
        }
    }

    /**
     * Some browser don't put <style> tags in the HEAD automatically (e.g. Opera)
     * This needs to be taken care of here
     * @method moveStyles
     */
    function moveStyles() {
        log("moving styles manually in the head");
        // put styles manually in head of document
        // FF does this automatically, Opera does not
        
        // get styles
        var ycwStyles = vitaContainerEl.getElementsByTagName("style")[0];
        
        // copy styles
        var stylesCopy = ycwStyles.cloneNode(true);
        
        // put them in head of document
        document.getElementsByTagName("head")[0].appendChild(stylesCopy);
        
        // delete styles in ycw-container
        ycwStyles.parentNode.removeChild(ycwStyles);
    }
    
    /**
     * Store the x/y regions for each displayed vitality event in order to implement
     * hover behavior on items
     * @method storeRegions
     */
    function storeRegions() {
        eventItemEls = YUD.getElementsByClassName("hentry", "li", vitaContainerEl);
        
        if (eventItemEls.length > 0) {
            // store regions of every event item in order to be able to attach
            // "hover" class later on
            for (var i = 0, len = eventItemEls.length; i < len; i++) {
                itemRegions[i] = YUD.getRegion(eventItemEls[i]);
            }
        } else {
            log("no vitality events found", "error");
        }
    }

    /**
     * helper function for deleteEventHandler
     * @method getQueryStringParameter
     */
    function getQueryStringParameter(paramName, url) {
        params = parseQueryString(url);
        return params[paramName] === undefined ? null : params[paramName];
    }

    function parseQueryString(url, doDecode) {
        var idx, queryString, regexPat, splitParam;

        // decode the param value by default
        if (doDecode === undefined) doDecode = true;
        
        regexPat = /([^=&]+)=([^&]*)/g;
        idx = url.indexOf("?");
        queryString = idx >= 0 ? url.substr(idx + 1) : url;

        // Remove the hash if any
        idx = queryString.lastIndexOf("#");

        queryString = idx >= 0 ? queryString.substr(0, idx) : queryString;

        var result = {};
        var splitURL = queryString.split('&');
        for (var i = 0, len = splitURL.length; i < len; i++) {
            splitParam = splitURL[i].split('=', 2);
            if (splitParam.length != 2) continue; 
            result[splitParam[0]] = doDecode ? decodeURIComponent(splitParam[1]) : splitParam[1];
        }
        return result;
    }

    /**
     * helper function to get the hostname of an URL for deleteEventClickHandler
     * @method getHostname
     */
    function getHostname(url) {
        var delim,idx,len;
        delim = url.indexOf('/');
        idx = url.indexOf('/',delim+2);
        len = idx - (delim + 2);
        return url.substr(delim+2,len);
    }

    /**
     * YCW Version 1.7: Delete your own events.
     * @name deleteEventClickHandler
     * @author Harold Liss <hliss@yahoo-inc.com>
     */
    function deleteEventClickHandler(e) {
        // stop the click
        YUE.preventDefault(e);
        var del_anchor = YUE.getTarget(e);
        var message;
            
        // generate IDs for the confirm and cancel links
        var delete_id = 'ycw-delete-event-confirm';
        var cancel_id = 'ycw-delete-event-cancel';

        YUD.get('ycw-delete-event-confirm').href = del_anchor.href;
            
        // init, show bubble dialog
        try {
            var bubble = YCW.Profiles.Widgets.vitality.Bubble(del_anchor,message);
            bubble.show(0.5);
            //            var lastChild = YUD.getLastChild(del_anchor);
            //            YUD.insertAfter(bubble,lastChild);
        } catch(e) {
            // TODO: fail better?
            log(e);
        }
        
        // Remove any existing click listeners for bubbles
        YUE.removeListener(YUD.get(delete_id));
    
        // Event handler for confirm case
        YUE.addListener(YUD.get(delete_id),'click',function(e) {
            YUE.preventDefault(e);
            
            // Confirmation. Send request.
            var el,url,querystring,hostname,scripturl;
            el = YUE.getTarget(e);
            url = el.href;

            var params = parseQueryString(url, false);

            // need to use array notation instead of object notation
            // coz IE doesn't like params.source
            querystring = '?.crumb=' + params['_crumb'];
            querystring += '&guid=' + params['collectionID'];
            querystring += '&collectionID=' + params['collectionID'];
            querystring += '&collectionType=' + params['collectionType'];
            querystring += '&class=' + params['class'];
            querystring += '&source=' + params['source'];
            querystring += '&type=' + params['type'];
            querystring += '&suid=' + params['suid'];

            log("querystring=" + querystring);

            var processDeleteEventResponse = function(o) {
                var updateEl = YUD.getAncestorByTagName(del_anchor,'li');
                        
                // hide the bubble
                bubble.hide(0.5,false);
                        
                // fade out the event and destroy it.
                YUD.setStyle(updateEl,'z-index','0');
                var anim = new YCW.util.Anim(updateEl,{opacity:{to:0.0}});
                anim.onComplete.subscribe( function() {
                    var date_ul,children,prev_h4,parent_div;
                    date_ul = YUD.getAncestorByTagName(updateEl,'ul');
                    date_ul.removeChild(updateEl);
                    children = YUD.getChildren(date_ul);
                    if(children.length === 0) {
                        // all of this ul's lis are gone. remove it and its header.
                        prev_h4 = YUD.getPreviousSibling(date_ul);
                        parent_div = YUD.getAncestorByTagName(date_ul,'div');
                        parent_div.removeChild(date_ul);
                        parent_div.removeChild(prev_h4);
                    }
                });
                anim.animate();
            };


            var processDeleteEventFailure = function(o) {};

            // send the request
            scripturl = config.ycwHost + '/updates/V1/deleteUpdates' + querystring;
            YCW.util.Get.script(scripturl,{ onSuccess: processDeleteEventResponse, onFailure: processDeleteEventFailure });
        });
            
        // Event handler for cancel case - hides the confirmation
        YUE.addListener(YUD.get(cancel_id),'click', function(e) { 
            YUE.preventDefault(e);
            bubble.hide(0.5,false);
        });
    }

    /**
     * Modules main function
     * @method init
     * @param {Object} config configuration object for widget
     */
    function init(c) {
        log("widget.init");
        
        YUE.onAvailable("ycw-container", function () {
            // setting up params
            vitaContainerEl = YUD.get("ycw-container");
            itemActionsEl = YUD.get("ycw-item-actions");
            itemActionsOverlay = new YCW.widget.Overlay(itemActionsEl, {
                width: "150px",
                close: false,
                underlay: "none",
                iframe: false
            });
            config = c;
            
            if (vitaContainerEl.getElementsByTagName("style")[0]) {
                moveStyles();
            }
            
            // subscribe to frameMsg Handler
            // this is used in any case, because the invite kiosk iframe might
            // need to communicate with the parent
            if (YCW.util.CrossFrame) {
                YCW.util.CrossFrame.onMessageEvent.subscribe(YCW.Profiles.Widgets.vitality.parentPage.frameMsgHandler);
            } else {
                log("CrossFrame.js is not included, no x-frame communication possible", "error");
            }
            
            storeRegions();

            if (config.type === "iframe" || typeof config.type === "undefined") {
                log("iframe mode");
                // this is being called on the actual widget page
                // set proxy and send height to parent
                proxyUrl = config.proxyUrl;
                YUE.onAvailable("ycw-container", sendHeightToParent);
            }
            
            if (typeof config[".done"] === "undefined") {
                log(".done is not populated, doing it in JS");
                populateDone();
            }
            
            // create event listeners
            attachEventListeners();
            
            // render action item menu drop-down
            itemActionsOverlay.render();
        
            inviteEl = YUD.get("ycw-invite-tool-tip");
            inviteToolTipOverlay = new YCW.widget.Overlay(inviteEl, { close: false, underlay: "none", iframe: false, constraintoviewport: true});
            inviteToolTipOverlay.render();

            try  { pHost = YUD.get('ycw-invite-pHost').innerHTML; }
              catch(e)  { pHost = 'profiles.yahoo.com'; }

        });
    }
    
    return {
        init: init,
        sendHeight: sendHeightToParent,
        processSocDirResponse: processSocDirResponse,
        storeRegions: storeRegions
    };
}();

