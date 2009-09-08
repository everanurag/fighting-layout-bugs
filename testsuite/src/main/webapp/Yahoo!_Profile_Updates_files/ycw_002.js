/**
 * @requires yahoo, event
 * @namespace YCW.util
 * @title IFrame Message Routing Utility
 */

/* This is done here to allow the receive method to be usable when YCW is not
 * defined. This behaviour is used in xdm_proxy.php.
 */
if (typeof YCW === "undefined" || !YCW) {
    var YCW = {
        util: {},
        lang : {
            isFunction : function (x) {
                return typeof x === 'function';
            },
            isString : function (s) {
                return (''+s) === s && typeof s === 'string';
            },
            hasOwnProperty: function(o, prop) {
                if (Object.prototype.hasOwnProperty) {
                    return o.hasOwnProperty(prop);
                }

                return typeof o[prop] !== 'undefined' && o.constructor.prototype[prop] !== o[prop];
            }
        }
    };
}

/**
 * The CrossFrame singleton allows iframes to safely communicate even
 * if they are on different domains. This utility requires a xdm_proxy.php
 * file (xdm_proxy.php)
 *
 * @class CrossFrame
 */
YCW.util.CrossFrame = (function () {
    var MAX_SIZE = 2000,
        FRAMES_NUMERIC = /^frames\[(\d+)\]$/,
        FRAMES_NAMED = /^frames\[['"]([a-zA-Z0-9-_]+)['"]\]$/,
        $L = YCW.lang;

    /*
     * forEach array item run func
     */
    var forEach = function(arr, func) {
        for(var i=0, l=arr.length; i<l; i++) {
            func(arr[i], i);
        }
    };

    /*
     * mapReduce array item by running func with initial value.
     */
    var mapReduce = function(arr, func, res) {
        forEach(arr, function(item, index) {
            res = func(item, index, res);
        });
        return res;
    };

    /*
     * Parse a Query String into a Object.
     */
    var parseQueryString = function(s) {
        return mapReduce(s.split('&'), function(pair, index, result) {
            var e = pair.indexOf('=');
            result[pair.substr(0, e)] = decodeURIComponent(pair.substr(e+1));
            return result;
        }, {}); // {} => start with an empty object
    };

    /*
     * Convert a node reference into a string that can be used to target this
     * node.
     */
    var nodeToTarget = function(node) {
        if ($L.isString(node)) {
            return node;
        }
        if (node === window.parent) {
            return 'parent';
        }
        if (node === window.top) {
            return 'top';
        }
        if (node.tagName && node.tagName === 'IFRAME' && node.contentWindow) {
            // if the node has a name, use that
            if (node.name !== '') {
                return "frames['" + node.name + "']";
            }
            // dynamically generating a node name does not work
            // so we iterate through all iframe elements and use
            // a numeric index instead.
            //
            // this is not perfect because having a regular frame (vs iframe)
            // will throw this off. but who uses frames?
            else {
                var iframes = document.getElementsByTagName('iframe');
                for(var i=0, l=iframes.length; i<l; i++) {
                    if (iframes[i] === node) {
                        return "frames[" + i + "]";
                    }
                }
            }
        }
        return null;
    };

    /*
     * Convert a target string into a node reference.
     * Throws a Error if it fails.
     */
    var targetToNode = function(target) {

        // iframe node reference is special
        if (target.tagName && target.tagName === 'IFRAME' && target.contentWindow) {
            return target.contentWindow;
        }

        // split on .
        // each part must be one of:
        //  - parent
        //  - top
        //  - frames[\d+]
        //  - frames["[a-zA-Z\d-_]"]
        //  - frames['[a-zA-Z\d-_]']
        return mapReduce(target.split('.'), function(part, index, node) {
            if (part === 'parent') {
                return node.parent;
            }
            if (part === 'top') {
                return node.top;
            }

            var matches;
            if ((matches = FRAMES_NUMERIC.exec(part) || FRAMES_NAMED.exec(part))) {
                return node.frames[matches[1]];
            }

            throw new Error('Invalid Target Part: ' + part);
        }, window); // window => the initial node to start with
    };

    /*
     * Find a iframe node given a src url.
     *
     * NOTE: If multiple iframes with the same src are present, the first
     * matching one will be returned.
     */
    var findIframe = function(src) {
        var href = ($L.isString(src)) ? src.replace(/#$/, '') : false;
        var iframes = document.getElementsByTagName('iframe');
        for (var i = 0, l = iframes.length; i < l; i ++) {
            if (iframes[i].src.replace(/#$/, '') === href || iframes[i].contentWindow.location === src) {
                return iframes[i];
            }
        }
        return null;
    };

    /*
     * verify the random number in the message with the one 
     * that was created in loader
     */
     var isRandomValid = function(n) {
        return (n == YCW.Profiles.WidgetLoader.rand);
     };
     
     /*
      * This function makes sure that widget name passed in the cross-frame message is valid
      */
     var isWidgetNameValid = function(w) {
        widgetNames = ["photopicker", "nicknamepicker"];
        widgetNamesLen = widgetNames.length;
        
        // is the widget name valid?
        for(i=0; i<widgetNamesLen; i++) {
            if(w == widgetNames[i]) {
                return true;
            }
        } 
        
        return false;
     };
     
     /*
      * set of rules to validate the cross-frame message
      */
      var isMessageValid = function(msg) {
        var parts, ret, widgetNames, i;
        if(!$L.isString(msg)) return false;
        
        parts = msg.split("|");
        return (isWidgetNameValid(parts[0]) && isRandomValid(parts[1]));  
      };

    /*
     * This is a load-time fork that sets up a different _send function using
     * postMessage if available and falling back to the iframe+hash for others.
     */
    var _send;
	
    if (window.postMessage || document.postMessage) {
        // opera currently puts postMessage on document, not window
        var useDocument = !$L.isFunction(window.postMessage) && !window.attachEvent;

        var messageHandler = function(evt) {
            var iframe = findIframe(evt.source.location || evt.uri);
            var uri;
            if (iframe && iframe.src) {
                uri = iframe.src;
            } else {
                uri = evt.origin ? evt.origin : 'http://' + evt.domain;
            }

            var domain = uri.split('/')[2];
            
            // validate the message, and 
            // Let the application know a message has been received.
           
            //if(isMessageValid(evt.data)) {
                YCW.util.CrossFrame.onMessageEvent.fire(evt.data, domain, uri, iframe);
            //}
        };

        // IE8 _still_ doesnt have DOM2 events
        if (window.addEventListener) {
            window.addEventListener('message', messageHandler, false);
        }
        else if (window.attachEvent) {
            window.attachEvent('onmessage', messageHandler);
        }

        _send = function(proxy, target, message) {
            var matches, targetNode;
            targetNode = targetToNode(nodeToTarget(target)); // explicitly not catching the error possibly thrown
            if (useDocument) {
                targetNode = targetNode.document;
            }
            // send it!

            if (!targetNode) {
                throw new Error('Could not resolve target: ' + target);
            }
            targetNode.postMessage(message, proxy);
        };
    }
    // no postMessage support
    else {
        var iframeOnload = function X (e, args) {
            // First, remove the event listener or the iframe
            // we intend to discard will not be freed...
            var el = this;
            YCW.util.Event.removeListener(el, 'load', X);

            window.setTimeout(function() {
                if (el.parentNode) {
                    el.parentNode.removeChild(el);
                }
                el = null;
                if (args.message_rest) {
                    _send(args.proxy, args.target, args.message_rest, args.key);
                }
            }, 20);
        };

        _send = function (proxy, target, message, key) {
            // old browser support. fake postMessage through a transient iframe
            // messages may need to be chunked

            target = nodeToTarget(target); // normalize target to string
            var el, s, message_rest;

            if (message.length > MAX_SIZE) {
                message_rest = message.substr(MAX_SIZE);
                message = message.substr(0,MAX_SIZE);
            }

            // generate a key common to each chunked packet, somewhat unlikely to collide
            // the collisions are purely client side
            key = key || (Math.random()*(1<<30)).toString(16);

            // Create a new hidden iframe.
            el = document.createElement('iframe');
            s = el.style;
            s.position = 'absolute';
            s.visibility = 'hidden';
            s.top = s.left = s.width = s.height = '0';
            document.body.appendChild(el);

            // Listen for the onload event.
            YCW.util.Event.addListener(el, 'load', iframeOnload, {
                proxy: proxy,
                target: target,
                message_rest: message_rest,
                key: key
            });

            // Compose the message...
            s = 'target='   + encodeURIComponent(target) +
                '&key='     + encodeURIComponent(key) +
                '&message=' + encodeURIComponent(message) +
                '&url='     + encodeURIComponent(window.location.toString());
            if (message_rest) {
                s += '&p=1';
            }

            // Set its src
            el.src = proxy + '#' + s;
        };
    }

    /*
     * This is called from xdm_proxy to process the current URL including the
     * hash which contains our message.
     */
    var proxyReceive = function() {
        var href = window.location.href,
            params = parseQueryString(href.substr(href.indexOf('#') + 1));

        if (
            $L.hasOwnProperty(params, 'target') &&
            $L.hasOwnProperty(params, 'message') &&
            $L.hasOwnProperty(params, 'key') &&
            $L.hasOwnProperty(params, 'url')
        ) {
            var target = targetToNode('parent.' + params.target),
                domain = params.url.split('/')[2],
                buffer = target.YCW.util.CrossFrame.buffer,
                key = params.url + '|' + params.key;

            if ($L.hasOwnProperty(params, 'p')) {
                if (!buffer[key]) {
                    buffer[key] = [];
                }
                buffer[key].push(params.message);
                return; // partial message, just wait for the rest
            } else if (buffer[key]) {
                // last part of a multi-part message
                params.message = [].join.call(buffer[key], '') + params.message;
                delete buffer[key];
            }

            if (!target) {
                throw new Error('Could not resolve target: ' + target);
            }

            // Let the application know a message has been received.
            target.YCW.util.CrossFrame._receive(params.message, domain, params.url);
        } else {
            throw new Error('Missing required params: ' + location.hash.substr(1));
        }
    };

    /**
     * If CustomEvent is not found, we fallback to only providing the
     * proxyReceive logic. This allows this file to also work without requiring
     * YUI for xdm_proxy.php
     */
    var CrossFrame;
    if (YCW.util.CustomEvent) {
        CrossFrame = {
            /**
             * Fired when a message is received.
             *
             * @event onMessageEvent
             */
            onMessageEvent: new YCW.util.CustomEvent('onMessage'),

            /**
             * Temporary receiving buffer, to handle chunking.
             */
            buffer: {},

            /**
             * Sends a message to an iframe, using the specified proxy.
             *
             * @method send
             * @param {string} proxy Complete path to the proxy file.
             * @param {string} target Target iframe e.g: parent.frames['foo']
             * @param {string} message The message to send.
             */
            send: _send,

            /**
             * Recieves a message from an iframe. For internal use only.
             */
            _receive: function(message, domain, uri) {
                //if(isMessageValid(message)) {
                    YCW.util.CrossFrame.onMessageEvent.fire(message, domain, uri, findIframe(uri));
                //}
            },

            proxyReceive: proxyReceive
        };
    }
    else {
        CrossFrame = {
            proxyReceive: proxyReceive
        };
    }

    return CrossFrame;
})();
