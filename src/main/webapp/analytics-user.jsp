
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->

    <body>

    <script src="assets/js/jquery-1.11.3.js"></script>
    <script src="assets/js/jquery-ui.js"></script>
    <script src="assets/js/moment-with-locales.js"></script>
    <script src="assets/js/jquery.jscroll.js"></script>
    <script src="assets/js/d3.v3.js" charset="utf-8"></script>
    <%--<link href="assets/css/justified-nav.css" rel="stylesheet">--%>
    <link href="assets/css/bootstrap.min.css" rel="stylesheet">
    <style> /* set the CSS */

    body { font: 12px Arial;}

    path {
        stroke: steelblue;
        stroke-width: 3;
        fill: none;
    }
    path[data-type="likes"] {
        fill: none;
        stroke: #f5cb00;
        stroke-width: 2;
        stroke-opacity: 1;
        stroke-linecap: round;
        stroke-linejoin: round;
    }

    path[data-type="follows"] {
        fill: none;
        stroke: #00cbf5;
        stroke-width: 2;
        stroke-opacity: 1;
        stroke-linecap: round;
        stroke-linejoin: round;
    }

    .axis path,
    .axis line {
        fill: none;
        stroke: grey;
        stroke-width: 2;
        shape-rendering: crispEdges;


    }

    .axis .tick {
        stroke: lightgrey; stroke-opacity: 0.2; stroke-width: 0.5; shape-rendering: crispEdges;
    }
    .grid path {
        stroke-width: 0;
    }

    .overlay {
        fill: none;
        pointer-events: all;
    }

    .focus circle {
        fill: none;
        stroke: steelblue;
    }

    </style>
    <body>

    <!-- load the d3.js library -->
    <!--script src="http://d3js.org/d3.v3.min.js"></script-->

    <script type="text/javascript">
        function getParameterByName(name) {
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
                    results = regex.exec(location.search);
            return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
        };

    </script>

    <script>

        // Set the dimensions of the canvas / graph
        var margin = {top: 30, right: 100, bottom: 30, left: 150},
                width = 900 - margin.left - margin.right,
                height = 200 - margin.top - margin.bottom;

        // Parse the date / time
        var parseDate = d3.time.format("%m%d%y").parse;

        // Set the ranges
        //var x = d3.scale.linear().range([0, width]);
        var x_1 = d3.time.scale().range([0, width]);
        var y_1 = d3.scale.linear().range([height, 0]);

//        var x_2 = d3.scale.ordinal()
//                .domain(["P1M", "P15D", "P10D", "P7D", "P5D", "P3D", "P2D", "P1D"])
//                .rangePoints([0, 100]);
        //var x_2 = d3.scale.linear().domain([31,30,15,10,7,5,3,2,1]).range([0, width]);
        var x_2 = d3.scale.linear().range([width,0]);
        //var y_2 = d3.scale.linear().range([height, 0]);
        var y_2 = d3.scale.linear().range([height,0]);

        //    var xScale = d3.time.scale.utc()
        //            .domain([beginning, ending])
        //            .range([margin.left, availableWidth - margin.right]);
        //    // Define the axes
        /*
         var xAxis = d3.svg.axis().scale(x)
         .orient("bottom").ticks(5);
         */
        //var xAxis = d3.svg.axis().scale(x).orient("bottom").ticks(d3.time.day,1);
        var xAxis_1 = d3.svg.axis().scale(x_1).orient("bottom").ticks(d3.time.week,1)
                .tickFormat(d3.time.format('P%mW%W'))
                .tickSize(8)
                .tickPadding(8);

        var yAxis_1 = d3.svg.axis().scale(y_1)
                .orient("left").ticks(5);



        var xAxis_2 = d3.svg.axis().scale(x_2).orient("bottom")
                //.ticks(d3.time.week,1)
                //.tickFormat(d3.time.format('P%mW%W'))
                .tickSize(8)
                .tickPadding(8);

        var yAxis_2 = d3.svg.axis().scale(y_2)
                .orient("left").ticks(8);

        // Define the line
        /*
         var valueline = d3.svg.line()
         .x(function(d) { return x(d.date); })
         .y(function(d) { return y(d.close); });
         */

        var followsLine_1 = d3.svg.line()
                .interpolate("basis")
                .y(function(d) {
                    //console.debug(Number(d["counts"]["followed_by"]));
                    var f = Number(d["counts"]["followed_by"]);
                    //console.debug(y(f));
                    return y_1(f);
                })
                .x(function(d) {
//                console.debug(Number(d["snapshot-day-of-year"]));

                    var d = d["snapshot-date"];
                    return x_1(d);
                });

        var likesLine = d3.svg.line()
                .interpolate("basis")
                .y(function(d) {
                    //console.debug(Number(d["analytics"]["engagement-analytics-meta"]["avg-likes"]));
                    var f = Number(d["analytics"]["engagement-analytics-meta"]["avg-likes"]);
                    console.debug(y_2(f));
                    return y_2(f);
                })
                .x(function(d) {
//                console.debug(Number(d["snapshot-day-of-year"]));

                    //var f = new Date(d["period-coverage-end-millis"]);
                    var f = Number(d["snapshot-coverage-duration-days"]);//parseDate(d["snapshot-date"]);
                    if(isNaN(f)) {
                        var s = d["snapshot-coverage-period"];
                        var p = s.substring(1, s.length-1);
                        if(s == "P1M") {
                            return x_2(30);
                        } else {
                            return x_2(Number(p));
                        }
                    }
                    return x_2(f);
                });

        var commentsLine = d3.svg.line()
                .y(function(d) {
                    console.debug(Number(d["analytics"]["engagement-analytics-meta"]["avg-comments"]));
                    var f = Number(d["analytics"]["engagement-analytics-meta"]["avg-comments"]);
                    //console.debug(y(f));
                    return y_2(f);
                })
                .x(function(d) {
                    var f = d["snapshot-coverage-duration-days"];
                    return x_2(f);
                });

        var username = getParameterByName("username");


        $(document).ready(function() {

            // Adds the svg canvas
            var chart_1 = d3.select("#chart")
                    .append("svg")
                    .attr("width", width + margin.left + margin.right)
                    .attr("height", height + margin.top + margin.bottom)
                    .append("g")
                    .attr("transform",
                            "translate(" + margin.left + "," + margin.top + ")");

            var chart_2 = d3.select("body")
                    .append("svg")
                    .attr("width", width + margin.left + margin.right)
                    .attr("height", height + margin.top + margin.bottom)
                    .append("g")
                    .attr("transform",
                            "translate(" + margin.left + "," + margin.top + ")");


            $("#username").replaceWith("<h1>@"+username+"</h1>");

            d3.json("/rest/analytics/get/user/" + username, function (error, json) {
                if (error) return console.warn(error);
                data = json;
                data.forEach(function (d) {
                    //console.info(d);
                    d["snapshot-date"] = parseDate(d["snapshot-date"]); //Number(d["snapshot-date"]);
                    d["followed_by"] = Number(d["followed_by"]);
                });

                x_1.domain(d3.extent(data, function (d) {
                            return d["snapshot-date"];
                        }
                ));

                y_1.domain(d3.extent(data, function (d) {
                    return Number(d["counts"]["followed_by"]);
                }));

                chart_1.append("path")
                        .attr("data-type", "follows")
                        .attr("class", "line")
                        .attr("d", followsLine_1(data));

                // Add the X Axis
                chart_1.append("g")
                        .attr("class", "x axis")
                        .attr("transform", "translate(0," + height + ")")
                        .call(xAxis_1);

                // Add the Y Axis
                chart_1.append("g")
                        .attr("class", "y axis")
                        .call(yAxis_1);

                chart_1.append("text")
                        .attr("x", (width / 2))
                        .attr("y", -10)
                        .attr("text-anchor", "middle")
                        .style("font-size", "16px")
                        .style("text-decoration", "underline")
                        .text("Followers")


                var focus = chart_1.append("g")
                        .attr("class", "focus")
                        .style("display", "none");

                focus.append("circle")
                        .attr("r", 4.5);

                focus.append("text")
                        .attr("x", 9)
                        .attr("dy", ".35em");

                chart_1.append("rect")
                        .attr("class", "overlay")
                        .attr("width", width)
                        .attr("height", height)
                        .on("mouseover", function() { focus.style("display", null); })
                        .on("mouseout", function() { focus.style("display", "none"); })
                        .on("mousemove", mousemove);

                function mousemove() {
                    //var x0 = x.invert(d3.mouse(this)[0]),
//                            i = bisectDate(data, x0, 1),
//                            d0 = data[i - 1],
//                            d1 = data[i],
//                            d = x0 - d0.date > d1.date - x0 ? d1 : d0;
                    //focus.attr("transform", "translate(" + x(d.date) + "," + y(d.close) + ")");
                    focus.attr("transform", "translate(100,100  )");
                    //focus.select("text").text(formatCurrency(d.close));
                    focus.select("text").text("Hello");
                }


            });

            d3.json("/rest/analytics/get/aggregate-period-data/user/" + username, function (error, json) {
                if (error) return console.warn(error);
                var a = [];
                data = json;
                data.forEach(function (d) {
                    d["snapshot-date"] = parseDate(d["snapshot-date"]); //Number(d["snapshot-date"]);
                    d["analytics"]["engagement-analytics-meta"]["avg-likes"] = Number(d["analytics"]["engagement-analytics-meta"]["avg-likes"]);
                    d["analytics"]["engagement-analytics-meta"]["avg-comments"] = Number(d["analytics"]["engagement-analytics-meta"]["avg-comments"]);
                    a.push(d["analytics"]["engagement-analytics-meta"]["avg-likes"]);
                });


                x_2.domain(d3.extent(data, function (d) {
                    //return d["period-coverage-end-millis"];
                    var f = d["snapshot-coverage-duration-days"];
                    if (isNaN(f)) {
                        var s = d["snapshot-coverage-period"];
                        var p = s.substring(1, s.length - 1);
                        if (s == "P1M") {
                            return 30;
                        } else {
                            return Number(p);
                        }
                    } else {
                        return d["snapshot-coverage-duration-days"];
                    }
                }));


                y_2.domain([0, d3.max(a)]);

                chart_2.append("svg:path")
                        .attr("data-type", "likes")
                        .attr("d", likesLine(data));

                // Add the X Axis
                chart_2.append("g")
                        .attr("class", "x axis")
                        .attr("transform", "translate(0," + height + ")")
                        .call(xAxis_2);

                // Add the Y Axis
                chart_2.append("g")
                        .attr("class", "y axis")
                        .call(yAxis_2);




            });

        });


     </script>
<script>
    $(document).ready(function(){
       $("#username").css("background-color","yellow");
        document.title = username;
    });
</script>


    <title>Analytics User</title>
    <div id="username">Hello</div>


    <div id="chart"></div>
</body>
