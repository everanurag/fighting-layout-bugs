//-*- coding: utf-8 -*-
// Copyright (c) 2012 by Oliver Lau <ola@ct.de>, Heise Zeitschriften Verlag
// Alle Rechte vorbehalten.
// $Id$


var Clock = (function() {

    var settings = {
        scene: {
            width: 320,
            height: 800,
            container: "#clock-container"
        },
        face: {
            radius: 125
        },
        tick: {
            width: 10,
            height: 20
        },
        hour_hand: {
            width: 12,
            height: 75
        },
        minute_hand: {
            width: 8,
            height: 110
        },
        second_hand: {
            width: 3,
            height: 115
        },
        axis: {
            radius: 4
        },
        pendulum: {
            width: 50,
            height: 500
        }
    },
    x0 = settings.scene.width / 2,
    y0 = 180;


    function hoursToDegrees(h) {
        return 360 * h / 12 - 180;
    }


    function minutesToDegrees(m) {
        return 360 * m / 60 - 180;
    }


    function secondsToDegrees(s) {
        return 360 * s / 60 + 180;
    }


    function stopAnimation() {
        $("#second-hand").removeClass("move");
        $("#minute-hand").removeClass("move");
        $("#hour-hand").removeClass("move");
        $("#pendulum-container").removeClass("move");
    }


    function startAnimation() {
        $("#second-hand").addClass("move");
        $("#minute-hand").addClass("move");
        $("#hour-hand").addClass("move");
        $("#pendulum-container").addClass("move");
    }


    function css_omnify(id, key, value) {
        $.each(["o", "moz", "webkit"], function() {
            $(id).css("-"  + this + "-" + key, value);
        });
    }


    function setHours(h) {
        var xoff = (x0 - settings.hour_hand.width/2),
        yoff = (y0 - settings.hour_hand.width/2);
        h = hoursToDegrees(h);
        css_omnify("#hour-hand", "transform-origin",
                   (settings.hour_hand.width/2) + "px " + (settings.hour_hand.width/2) + "px");
        $("#hours")
            .replaceWith("<style id=\"hours\" type=\"text/css\">\n" +
                         $.map(["o", "moz", "webkit"],
                               function(browser) {
                                   return "@-" + browser + "-keyframes move-hour-hand {\n" +
                                       "  0% { -" + browser + "-transform: "+
                                       "translate(" + xoff + "px, " + yoff + "px)" +
                                       "rotate(" + h + "deg); " +
                                       "}\n" +
                                       "  100% { -" + browser + "-transform: " +
                                       "translate(" + xoff + "px, " + yoff + "px)" +
                                       "rotate(" + (h+360) + "deg); " +
                                       "}\n" +
                                       "}\n";
                               }).join("") +
                         "</style>");
    }


    function setMinutes(m) {
        var xoff = (x0 - settings.minute_hand.width/2),
        yoff = (y0 - settings.minute_hand.width/2);
        m = minutesToDegrees(m);
        css_omnify("#minute-hand", "transform-origin",
                   (settings.minute_hand.width/2) + "px " + (settings.minute_hand.width/2) + "px");
        $("#minutes")
            .replaceWith("<style id=\"minutes\" type=\"text/css\">" +
                         $.map(["o", "moz", "webkit"],
                               function(browser) {
                                   return "@-" + browser + "-keyframes move-minute-hand {\n" +
                                       "  0% { -" + browser + "-transform: " +
                                       "translate(" + xoff + "px, " + yoff + "px)" +
                                       "rotate(" + m + "deg); " +
                                       "}\n" +
                                       "  100% { -" + browser + "-transform: " +
                                       "translate(" + xoff + "px, " + yoff + "px)" +
                                       "rotate(" + (m+360) + "deg); " +
                                       "}\n" +
                                       "}\n";
                               }).join("") +
                         "</style>");
    }


    function setSeconds(s) {
        var xoff = (x0 - settings.second_hand.width/2),
        yoff = (y0 - settings.second_hand.width/2);
        s = secondsToDegrees(s);
        css_omnify("#second-hand", "transform-origin",
                   (settings.second_hand.width/2) + "px " + (settings.second_hand.width/2) + "px");
        $("#seconds")
            .replaceWith("<style id=\"seconds\" type=\"text/css\">" +
                         $.map(["o", "moz", "webkit"],
                               function(browser) {
                                   return "@-" + browser + "-keyframes move-second-hand {\n" +
                                       (function() {
                                           var i, percent, style = "", hold = 0.90 / 60, deg;
                                           for (i = 0; i < 60; ++i) {
                                               percent = 100 * i / 60;
                                               deg = s + 6 * i;
                                               style += percent + "% { -" + browser + "-transform: " +
                                                   "translate(" + xoff + "px, " + yoff + "px) " +
                                                   "rotate(" + deg + "deg);" +
                                                   "}\n" +
                                                   (percent + 1.592) + "% { -" + browser + "-transform: " +
                                                   "translate(" + xoff + "px, " +  yoff + "px) " +
                                                   "rotate(" + deg + "deg);" +
                                                   "}\n";
                                           }
                                           return style;
                                       })() +
                                       "}";
                               }).join("\n") +
                         "</style>");
        /* ---- ALTERNATIVE 1 -----
        $("#seconds")
            .replaceWith("<style id=\"seconds\" type=\"text/css\">" +
                         $.map(["o", "moz", "webkit"],
                               function(browser) {
                                   return "@-" + browser + "-keyframes move-second-hand {\n" +
                                       "0% { -" + browser + "-transform: " +
                                       "  translate(" + xoff + "px, " + yoff + "px) " +
                                       "  rotate(" + s + "deg);" +
                                       " }\n" +
                                       "100% { -" + browser + "-transform: " +
                                       "  translate(" + xoff + "px, " + yoff + "px) " +
                                       "  rotate(" + (s+360) + "deg);" +
                                       " }\n" +
                                       "}";
                               }).join("\n") +
                         "</style>");
            */
        /* ---- ALTERNATIVE 2 -----
        $("#seconds")
            .replaceWith("<style id=\"seconds\" type=\"text/css\">" +
                         $.map(["o", "moz", "webkit"],
                               function(browser) {
                                   return "@-" + browser + "-keyframes move-second-hand {\n" +
                                       (function() {
                                           var i, percent, style = "";
                                           for (i = 0; i < 60; ++i) {
                                               percent = 100 * i / 60;
                                               style += percent + "% { -" + browser + "-transform: " +
                                                   "translate(" + xoff + "px, " + yoff + "px) " +
                                                   "rotate(" + (s + 6 * i) + "deg);" +
                                                   "}\n";
                                           }
                                           return style;
                                       })() +
                                       "}";
                               }).join("\n") +
                         "</style>");
            break;
            */
    }


    function setCurrentTime() {
        var t = new Date;
        setSeconds(t.getSeconds());
        setMinutes(t.getMinutes() + t.getSeconds() / 60);
        setHours(t.getHours() + t.getMinutes() / 60 + t.getSeconds() / 3600);
    }


    function generateClock() {
        $("head")
            .append("<style id=\"pendulum\" type=\"text/css\">" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "@-" + browser + "-keyframes move-pendulum {" +
                                  "  0% { -" + browser + "-transform: " +
                                  "translate(" +         
                                  (x0 - settings.pendulum.width/2) + "px, " + 
                                  (y0 - settings.pendulum.width/2) + "px)" +
                                  "rotate(10deg); " +
                                  "}\n" +
                                  "  50% { -" + browser + "-transform: " +
                                  "translate(" +         
                                  (x0 - settings.pendulum.width/2) + "px, " + 
                                  (y0 - settings.pendulum.width/2) + "px)" +
                                  "rotate(-10deg); " +
                                  "}\n" +
                                  "  100% { -" + browser + "-transform: " +
                                  "translate(" +         
                                  (x0 - settings.pendulum.width/2) + "px, " + 
                                  (y0 - settings.pendulum.width/2) + "px)" +
                                  "rotate(10deg); " +
                                  "}\n" +
                                  "}";
                          }).join("\n") + 
                    "</style>");
        $(settings.scene.container)
            .append("<div id=\"pendulum-container\" style=\"" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "-" + browser + "-transform: translate(" +
                                  (x0 - settings.pendulum.width/2) + "px, " + 
                                  (y0) + "px)";
                          }).join(";") +
                    "\"></div>" +
                    "<div id=\"face\" style=\"" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "-" + browser + "-transform: " +
                                  "translate(" +
                                  (x0 - settings.face.radius) + "px, " +
                                  (y0 - settings.face.radius) + "px)";
                          }).join(";") +
                    "\"></div>" +
                    (function() {
                        var style = "";
                        for (h = 1; h <= 12; ++h) {
                            style +="<div class=\"hour-mark\" style=\"" +
                                $.map(["o", "moz", "webkit"],
                                      function(browser) {
                                          return "-" + browser + "-transform: " +
                                              "translate(" +
                                              (x0 - settings.tick.height/2) + "px, " +
                                              (y0 - settings.tick.width/2) + "px)" +
                                              "rotate(" + hoursToDegrees(h) + "deg) " +
                                              "translate(" + (settings.face.radius-13) + "px, 0px)";
                                      }).join(";") + 
                                "\"></div>";
                        }
                        return style;
                    })() +
                    "<div id=\"hour-hand\" style=\"" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "-" + browser + "-transform: translate(" +
                                  (x0 - settings.hour_hand.width/2) + "px, " + 
                                  (y0 - settings.hour_hand.width/2) + "px)";
                          }).join(";") +
                    "\"></div>" +
                    "<div id=\"minute-hand\" style=\"" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "-" + browser + "-transform: translate(" +
                                  (x0 - settings.minute_hand.width/2) + "px, " + 
                                  (y0 - settings.minute_hand.width/2) + "px)";
                          }).join(";") +
                    "\"></div>" +
                    "<div id=\"second-hand\" style=\"" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "-" + browser + "-transform: translate(" +
                                  (x0 - settings.second_hand.width/2) + "px, " + 
                                  (y0 - settings.second_hand.width/2) + "px)";
                          }).join(";") +
                    "\"></div>" +
                    "<div id=\"axis\" style=\"" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "-" + browser + "-transform: translate(" +
                                  (x0 - settings.axis.radius/2) + "px, " + 
                                  (y0 - settings.axis.radius/2) + "px)";
                          }).join(";")+ 
                    "\"></div>");
        $("#pendulum-container")
            .append("<div class=\"pendulum\" style=\"" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "-" + browser + "-transform: translate(" +
                                  "21px, " + 
                                  "0px); ";
                          }).join("") +
                    "\"></div>" +
                    "<div class=\"weight\" style=\"" +
                    $.map(["o", "moz", "webkit"],
                          function(browser) {
                              return "-" + browser + "-transform: translate(" +
                                  "0px, " + 
                                  "460px); ";
                          }).join("") +
                    "\"></div>");
    }
    

    return {
        init: function() {
            $("body").addClass("loaded");
            $("#clock-container").addClass("loaded");
            generateClock();
            setCurrentTime();
            function onAnimation(event) {
                var e = event.originalEvent;
                console.log([ e.type, e.animationName, "t = " + e.elapsedTime + "s" ]);
            }
            $("#pendulum-container").bind({
                animationstart: function(e) { onAnimation(e); },
                animationend: function(e) { onAnimation(e); },
                animationiteration: function(e) { onAnimation(e); }
            });
            startAnimation();
            $("#start-stop-button").click(function() {
                if ($("#start-stop-button").text() == "Start") {
                    $("#pendulum-container").css("-webkit-animation-play-state", "running");
                    $("#hour-hand").css("-webkit-animation-play-state", "running");
                    $("#minute-hand").css("-webkit-animation-play-state", "running");
                    $("#second-hand").css("-webkit-animation-play-state", "running");
                    $("#start-stop-button").text("Stop");
                    setCurrentTime();
                    startAnimation();
                }
                else {
                    $("#pendulum-container").css("-webkit-animation-play-state", "paused");
                    $("#hour-hand").css("-webkit-animation-play-state", "paused");
                    $("#minute-hand").css("-webkit-animation-play-state", "paused");
                    $("#second-hand").css("-webkit-animation-play-state", "paused");
                    $("#start-stop-button").text("Start");
                    stopAnimation();
                }
            });
        }
    };
})();
