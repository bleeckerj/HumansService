<%--
  Created by IntelliJ IDEA.
  User: julian
  Date: 12/24/15
  Time: 3:37 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>



<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <script src="//code.jquery.com/jquery-1.11.3.min.js"></script>
    <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
    <script src="//s.cdpn.io/3/clamp.js"></script>
    <script src="assets/js/moment-with-locales.js"></script>
    <script src="assets/js/jquery.jscroll.js"></script>
    <script type="text/javascript">

        Number.prototype.toFixedDown = function(digits) {
            var re = new RegExp("(\\d+\\.\\d{" + digits + "})(\\d)"),
                    m = this.toString().match(re);
            return m ? parseFloat(m[1]) : this.valueOf();
        };

        // http://css-tricks.com/line-clampin/

        // Clamp.js
        // https://github.com/josephschmitt/Clamp.js

        /**
         * TextOverflowClamp.js
         *
         * Updated 2013-05-09 to remove jQuery dependancy.
         * But be careful with webfonts!
         */

        // bind function support for older browsers without it
        // https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Function/bind
        if (!Function.prototype.bind) {
            Function.prototype.bind = function (oThis) {
                if (typeof this !== "function") {
                    // closest thing possible to the ECMAScript 5 internal IsCallable function
                    throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
                }

                var aArgs = Array.prototype.slice.call(arguments, 1),
                        fToBind = this,
                        fNOP = function () {},
                        fBound = function () {
                            return fToBind.apply(this instanceof fNOP && oThis
                                            ? this
                                            : oThis,
                                    aArgs.concat(Array.prototype.slice.call(arguments)));
                        };

                fNOP.prototype = this.prototype;
                fBound.prototype = new fNOP();

                return fBound;
            };
        }

        // the actual meat is here
        (function(w, d){
            var clamp, measure, text, lineWidth,
                    lineStart, lineCount, wordStart,
                    line, lineText, wasNewLine,
                    ce = d.createElement.bind(d),
                    ctn = d.createTextNode.bind(d);

            // measurement element is made a child of the clamped element to get it's style
            measure = ce('span');

            (function(s){
                s.position = 'absolute'; // prevent page reflow
                s.whiteSpace = 'pre'; // cross-browser width results
                s.visibility = 'hidden'; // prevent drawing
            })(measure.style);

            clamp = function (el, lineClamp) {
                // make sure the element belongs to the document
                el = document.getElementById("js-clamp");
                if(!el.ownerDocument || !el.ownerDocument === d) return;
                // reset to safe starting values
                lineStart = wordStart = 0;
                lineCount = 1;
                wasNewLine = false;
                lineWidth = el.clientWidth;
                // get all the text, remove any line changes
                text = (el.textContent || el.innerText).replace(/\n/g, ' ');
                // remove all content
                while(el.firstChild !== null)
                    el.removeChild(el.firstChild);
                // add measurement element within so it inherits styles
                el.appendChild(measure);
                // http://ejohn.org/blog/search-and-dont-replace/
                text.replace(/ /g, function(m, pos) {
                    // ignore any further processing if we have total lines
                    if(lineCount === lineClamp) return;
                    // create a text node and place it in the measurement element
                    measure.appendChild(ctn(text.substr(lineStart, pos - lineStart)));
                    // have we exceeded allowed line width?
                    if(lineWidth < measure.clientWidth) {
                        if(wasNewLine) {
                            // we have a long word so it gets a line of it's own
                            lineText = text.substr(lineStart, pos + 1 - lineStart);
                            // next line start position
                            lineStart = pos + 1;
                        } else {
                            // grab the text until this word
                            lineText = text.substr(lineStart, wordStart - lineStart);
                            // next line start position
                            lineStart = wordStart;
                        }
                        // create a line element
                        line = ce('span');
                        // add text to the line element
                        line.appendChild(ctn(lineText));
                        // add the line element to the container
                        el.appendChild(line);
                        // yes, we created a new line
                        wasNewLine = true;
                        lineCount++;
                    } else {
                        // did not create a new line
                        wasNewLine = false;
                    }
                    // remember last word start position
                    wordStart = pos + 1;
                    // clear measurement element
                    measure.removeChild(measure.firstChild);
                });
                // remove the measurement element from the container
                el.removeChild(measure);
                // create the last line element
                line = ce('span');
                // give styles required for text-overflow to kick in
                (function(s){
                    s.display = 'block';
                    s.overflow = 'hidden';
                    s.textOverflow = 'ellipsis';
                    s.whiteSpace = 'nowrap';
                    s.width = '100%';
                })(line.style);
                // add all remaining text to the line element
                line.appendChild(ctn(text.substr(lineStart)));
                // add the line element to the container
                el.appendChild(line);
            }
            w.clamp = clamp;
        })(window, document);

        // the only bit of jQuery: trigger after fonts etc. are ready
//        $(window).bind('load', function() {
//            // Clamp.js
//            $clamp(document.getElementById("js-clamp"), {clamp: 3});
//            // TextOverflowClamp.js
//            clamp(document.getElementById('js-toclamp'), 3);
//        });


   </script>

    <script type="text/javascript">
        function getParameterByName(name) {
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
                    results = regex.exec(location.search);
            return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
        };

    </script>

    <script type="text/javascript">
        $(function() {
            $("input[type=submit], a, button")
                    .button()
                    .click(function (event) {
                        event.preventDefault();
                        var sort_by = getParameterByName("sortby");
                        if(sort_by == '') { sort_by = 'avg-likes';}

                        var date = getParameterByName('date');
                        if(date == '') { date = yesterday.format("MMDDYY"); }

                        var top = Number(getParameterByName('top'));
                        if(top == '') { top = 10;}
                        top+=10;
                        $.getJSON( "/rest/analytics/top/"+top+"-"+(top+10)+"/by/"+sort_by+"/date/"+date, function( json ) {
                            json.reports.forEach(function(analytic) {
                                var username = analytic["username"];
                                $("#infinite-scroll").html("<strong>"+username+"</strong>");
                            });
                        });


                    });
        });


    </script>

    <script type="text/javascript">



        $(document).ready(function() {

            $('.infinite-scroll').jscroll({
                autoTrigger: true
            });


            $('.grid-nav li a').on('click', function(event) {
                event.preventDefault();
                $('.grid-container').fadeOut(500, function () {
                    $('#' + gridID).fadeIn(500);
                });
                var gridID = $(this).attr("data-id");

                $('.grid-nav li a').removeClass("active");
                $(this).addClass("active");
            });


            var rest_url = '';
            var yesterday = moment().subtract(1, 'days');
            var sort_by = getParameterByName("sortby");
            if(sort_by == '') { sort_by = 'avg-likes';}
            var top = getParameterByName('top');
            if(top == '') { top = 10;}
            var date = getParameterByName('date');
            if(date == '') { date = yesterday.format("MMDDYY"); }
            var username = getParameterByName("username");
            if(username == '') {
                rest_url = "/rest/analytics/top/"+top+"/by/"+sort_by+"/date/"+date;

            } else {
                rest_url = "/rest/analytics/top/"+top+"/by/"+sort_by+"/user/"+username+"/date/"+date+"/period/P1D";
            }
            $.getJSON( rest_url, function( json ) {
                $("#title").append("<div class=\"page-header\"><h1>"+ json.Name +"</h1></div>");
                //console.log( "JSON Data: " + json["analytics"]["engagement-analytics-meta"]["period-posts-count"] );
                // $("#period-posts-count").html(json["analytics"]["engagement-analytics-meta"]["period-posts-count"]);
                var count = 1;
                json.reports.forEach(function(analytic) {
                    //console.log(analytic);
                    var avg_likes = analytic["analytics"]["engagement-analytics-meta"]["avg-likes"].toFixed(1);
                    var min_likes = analytic["analytics"]["engagement-analytics-meta"]["min-likes"].toFixed(1);
                    var max_likes = analytic["analytics"]["engagement-analytics-meta"]["max-likes"].toFixed(1);
                    var avg_comm = analytic["analytics"]["engagement-analytics-meta"]["avg-comments"].toFixed(1);
                    var min_comm = analytic["analytics"]["engagement-analytics-meta"]["min-comments"].toFixed(1);
                    var max_comm = analytic["analytics"]["engagement-analytics-meta"]["max-comments"].toFixed(1);
                    var avg_ment = analytic["analytics"]["engagement-analytics-meta"]["avg-mentions"].toFixed(1);
                    var avg_tags = analytic["analytics"]["engagement-analytics-meta"]["avg-tags"].toFixed(1);
                    var snapshot_coverage_period = analytic["snapshot-coverage-period"];
                    var period_posts_count = analytic["analytics"]["engagement-analytics-meta"]["period-posts-count"].toFixed(1);
                    var earliest_in_period = analytic["analytics"]["engagement-analytics-meta"]["earliest-in-period"];
                    var latest_in_period = analytic["analytics"]["engagement-analytics-meta"]["latest-in-period"];
                    var period_name = analytic["analytics"]["engagement-analytics-meta"]["period"];
                    var period_days = analytic["analytics"]["engagement-analytics-meta"]["period-days"].toFixed(1);
                    var posts_per_day = -1;


                        if (typeof analytic["analytics"]["engagement-analytics-meta"]["posts-per-day"] != "undefined") {
                            // Works
                            posts_per_day =  analytic["analytics"]["engagement-analytics-meta"]["posts-per-day"].toFixed(1);
                        } else
                        if (typeof analytic["analytics"]["engagement-analytics-meta"]["rate-posts-per-day"] != "undefined") {
                            posts_per_day = analytic["analytics"]["engagement-analytics-meta"]["rate-posts-per-day"].toFixed(1);
                        }

                    var image_content = "";//"<div class='row'>";
                    var content = "";
                    var col_count = 0;
                    var status_json = analytic["analytics"]["status-json"];
                    var item_count = 1;
                    status_json.forEach(function(status) {
                        //image_content += "<div class='col-sm-4 center-block' style='background-color:black; padding-left:0px; padding-right:0px; padding-bottom: 0px; padding-top: 3px'><img src='"+status["images"]["low_resolution"]["url"]+"' class='img-responsive'  /></div>";
                        //image_content+="<div class='col-sm-4 center block'>"+col_count+"</div>";
                        var text;
                        if(status["caption"] == undefined) {
                            text = "";
                        } else {
                            text = status["caption"]["text"];
                        }

                        image_content += "<li><img src='"+status["images"]["low_resolution"]["url"]+"'/><!--h3>Title</h3--><p id='js-toclamp'>"+text+"</p></li>";

                        col_count++;
                        if(col_count % 3 == 0) {
                            //x++;
                            if(item_count == status_json.length) {
                               // image_content += "</div>"
                            } else {
                               // image_content += "</div><div class='row'>";
                            }
                        }
                        item_count++;
                    });
//                    if(col_count % 3 != 0) {
//                        image_content += "X</div>";
//                    }
                    //image_content += "HELLO";
                    //image_content += "</tbody></table>";

                    var username = analytic["username"];
                    //var content = "<div class='col-sm-3'><ul class='list-group'><li class='list-group-item'>"+avg_likes+"</li></ul></div>"
                    var analytics_content = ""
                    analytics_content += "<div style='margin-top: 10px; margin-left: 10px;'><table class='table table-striped'><tbody>";
                    analytics_content += "<tr><td>period-posts-count</td><td>"+period_posts_count+"</td>";
                    analytics_content += "<td>period-days</td><td>"+period_days+"</td>";
                    analytics_content += "<td>posts-per-day</td><td>"+posts_per_day+"</td></tr>";
                    analytics_content += "<tr><td>Period</td><td>"+period_name+"</td>";
                    analytics_content += "<td>Coverage</td><td>"+snapshot_coverage_period+"</td></tr>";
                    analytics_content += "<tr><td>avg-likes</td><td>"+avg_likes+"</td>"
                    analytics_content += "<td>min-likes</td><td>"+min_likes+"</td>";
                    analytics_content += "<td>max-likes</td><td>"+max_likes+"</td></tr>";
                    analytics_content += "<tr><td>avg-comments</td><td>"+avg_comm+"</td>";
                    analytics_content += "<td>min-comments</td><td>"+min_comm+"</td>";
                    analytics_content += "<td>max-comments</td><td>"+max_comm+"</td></tr>";
                    analytics_content += "<tr><td>avg-mentions</td><td>"+avg_ment+"</td>";
                    analytics_content += "<td>avg-tags</td><td>"+avg_tags+"</td></tr>";
                    analytics_content += "<tr><td>Earliest Post in Period</td><td>"+earliest_in_period+"</td>";
                    analytics_content += "<td>Latest Post in Period</td><td>"+latest_in_period+"</td><td></td><td></td></tr></tbody></table></div>";
                    //content += "</div>";


                    // var foo = document.createElement("div");
                    //$("#something").append('<div class=\'row\'>WTF<div class=\'col-sm-4\'>');
                   // $("#something").append("<div class='panel panel-primary'><div class='panel-heading'><h3 class='panel-title'>#<b>"+(count++)+"</b> @"+username+"</h3></div></div><div class='panel-body' style='background-color:white'>"+image_content+"<div>"+content+"</div></div></div>");

                    content = "<div id=\"three-columns\" class=\"grid-container\"  style=\"display:block;\">";
                    content += "<p style=\"padding: 10 30 10; background: #f0f0f0; margin: 0 0 0 0.2%\">@"+analytic["username"]+"</p>";
                    content += "<ul class=\"rig columns-3\">";
                    //content += analytic["username"];
                    content+= image_content;
                    content+=analytics_content;

                    content+= "</ul></div>";
                    $("#something").append(content);
                    //$("#something").append("<div class='col-lg-12'>Hello Tuna!</div></div>");
                    //$("#something").append("<div class='col-lg-12'>"+content+"</div></div></div>");

                    //$("#something").append("#<b>"+(count++)+"</b> @"+username+"<div class='panel-body'><div class='col-lg-14'>"+image_content+"</div></div>");

                    //$("#something").append("</div></div>");
//                    $clamp(document.getElementById("js-clamp"), {clamp: 3});
//                    // TextOverflowClamp.js
//                    $clamp(document.getElementById('js-toclamp'), 3);
                });

            });
            //$("body").append("</div>");


        });

    </script>


    <title>Responsive Image Grids Using CSS</title>
    <link href='http://fonts.googleapis.com/css?family=Oswald' rel='stylesheet' type='text/css'>
    <style type="text/css">

        * {
            margin: 0;
            padding: 0;
        }
        body {
            background: url(images/noise_light-grey.jpg);
            font-family: 'Helvetica Neue', arial, sans-serif;
            font-weight: 200;
        }

        h1 {
            font-family: 'Oswald', sans-serif;
            font-size: 4em;
            font-weight: 400;
            margin: 0 0 20px;
            text-align: center;
            text-shadow: 1px 1px 0 #fff, 2px 2px 0 #bbb;
        }
        hr {
            border-top: 1px solid #ccc;
            border-bottom: 1px solid #fff;
            margin: 25px 0;
            clear: both;
        }
        .centered {
            text-align: center;
        }
        .wrapper {
            width: 100%;
            padding: 30px 0;
        }
        .container {
            width: 1200px;
            margin: 0 auto;
        }
        ul.grid-nav {
            list-style: none;
            font-size: .85em;
            font-weight: 200;
            text-align: center;
        }
        ul.grid-nav li {
            display: inline-block;
        }
        ul.grid-nav li a {
            display: inline-block;
            background: #999;
            color: #fff;
            padding: 10px 20px;
            text-decoration: none;
            border-radius: 4px;
            -moz-border-radius: 4px;
            -webkit-border-radius: 4px;
        }
        ul.grid-nav li a:hover {
            background: #7b0;
        }
        ul.grid-nav li a.active {
            background: #333;
        }
        .grid-container {
            display: none;
            padding: 0px 0px 30px 0px;
        }
        /* ----- Image grids ----- */
        ul.rig {
            list-style: none;
            font-size: 0px;
            margin-left: -0.0%; /* should match li left margin */
            margin-bottom: 30px;
        }
        ul.rig li {
            display: inline-block;
            padding: 8px;
            margin: 0 0 0.0% 0.1%;
            background: #fff;
            border: 1px solid #ddd;
            font-size: 12px;
            font-size: 1rem;
            vertical-align: top;
            box-shadow: 0 0 5px #ddd;
            box-sizing: border-box;
            -moz-box-sizing: border-box;
            -webkit-box-sizing: border-box;
        }
        ul.rig li img {
            max-width: 100%;
            height: auto;
            margin: 0 0 10px;
        }
        ul.rig li h3 {
            margin: 0 0 5px;
        }
        ul.rig li p {
            font-size: .9em;
            line-height: 1.5em;
            color: #999;
            height: 160px;
            overflow: hidden;
        }
        /* class for 2 columns */
        ul.rig.columns-2 li {
            width: 47.5%; /* this value + 2.5 should = 50% */
        }
        /* class for 3 columns */
        ul.rig.columns-3 li {
            width: 30.83%; /* this value + 2.5 should = 33% */
        }
        /* class for 4 columns */
        ul.rig.columns-4 li {
            width: 22.5%; /* this value + 2.5 should = 25% */
        }

        /*body {*/
            /*padding: 20px;*/
            /*font: 1.2em/1.2em 'Open Sans', sans-serif;*/
        /*}*/
        .module {
            border: 1px solid #CCC;
            width: 250px;
            margin: 0 0 1em 0;
            overflow: hidden;
        }
        .module p {
            margin: 0;
        }

        .line-clamp {
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
        }

        .fade {
            position: relative;
            height: 3.6em; /* exactly three lines */
        }
        .fade:after {
            content: "";
            text-align: right;
            position: absolute;
            bottom: 0;
            right: 0;
            width: 70%;
            height: 1.2em;
            background: linear-gradient(to right, rgba(255, 255, 255, 0), white 100%);
            pointer-events: none;
        }

        .last-line {
            height: 3.6em; /* exactly three lines */
            /*text-overflow: -o-ellipsis-lastline;*/
        }


        @media (max-width: 1199px) {
            .container {
                width: auto;
                padding: 0 10px;
            }
        }

        @media (max-width: 480px) {
            ul.grid-nav li {
                display: block;
                margin: 0 0 5px;
            }
            ul.grid-nav li a {
                display: block;
            }
            ul.rig {
                margin-left: 0;
            }
            ul.rig li {
                width: 100% !important; /* over-ride all li styles */
                margin: 0 0 20px;
            }
        }
    </style>
</head>

<body>

<div class="wrapper">
    <div class="container">
        <h1>Responsive Image Grids Using CSS</h1>
        <ul class="grid-nav">
            <li><a href="#" data-id="two-columns" >2 Columns</a></li>
            <li><a href="#" data-id="three-columns" class="active">3 Columns</a></li>
            <li><a href="#" data-id="four-columns">4 Columns</a></li>
        </ul>

        <hr />

        <div id="something"></div>




        <!--/#three-columns-->

        <div id="four-columns" class="grid-container">
            <ul class="rig columns-4">
                <li>
                    <img src="images/pri_001.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_002.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_003.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_004.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_005.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_006.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_007.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_008.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_009.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_010.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_011.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
                <li>
                    <img src="images/pri_012.jpg" />
                    <h3>Image Title</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </li>
            </ul>
        </div>
        <!--/#four-columns-->

        <hr />

        <p class="centered">Demo by <a href="http://alijafarian.com">Ali Jafarian</a></p>
    </div>
    <!--/.container-->
</div>
<!--/.wrapper-->



</body>
</html>
