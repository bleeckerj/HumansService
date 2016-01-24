<!DOCTYPE html>
<meta charset="utf-8">
<style> /* set the CSS */

body { font: 12px Arial;}

path {
    stroke: steelblue;
    stroke-width: 2;
    fill: none;
}

.axis path,
.axis line {
    fill: none;
    stroke: grey;
    stroke-width: 1;
    shape-rendering: crispEdges;
}

.legend {
    font-size: 16px;
    font-weight: bold;
    text-anchor: middle;
}

</style>
<body>

<!-- load the d3.js library -->
<script src="http://d3js.org/d3.v3.min.js"></script>

<script>

    // Set the dimensions of the canvas / graph
    var margin = {top: 30, right: 45, bottom: 70, left: 50},
            width = 960 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;

    // Parse the date / time
    var parseDate = d3.time.format("%Y-%m-%d %H:%M:%S").parse;
    formatDate = d3.time.format("%H:%M"),
            bisectDate = d3.bisector(function(d) { return d.date; }).left;

    // Set the ranges
    var x = d3.time.scale().range([0, width]);
    var y = d3.scale.linear().range([height, 0]);

    // Define the axes
    var xAxis = d3.svg.axis().scale(x)
            .orient("bottom");

    var yAxis = d3.svg.axis().scale(y)
            .orient("left").ticks(5);

    // Define the line
    var temperatureline = d3.svg.line()
            .x(function(d) { return x(d.date); })
            .y(function(d) { return y(d.temperature); });

    // Adds the svg canvas
    var svg = d3.select("body")
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform",
                    "translate(" + margin.left + "," + margin.top + ")");

    // set the url to retrieve data from sparkfun
    var inputURL = "http://data.sparkfun.com/output/OGz3bq3LxEiX3LovGq34.json?gt[timestamp]=now-1day";

    d3.json(inputURL, function(error, data) {

        data.forEach(function(d,i) {
            d.date = parseDate(d.dtg);
            d.temperature = +d.temperature;
            // Manual adjust for relative temperature differences
            // and to set the latest temp values
            // Return to pool
            if (d.sensor_id == '28-0000062c49a2')
            {
                d.temperature = d.temperature+0.75;
                if (i<=3) {toPool = d.temperature;}
            }

            // From pool
            if (d.sensor_id == '28-0000062c46e3')
            {
                d.temperature = d.temperature+0.0;
                if (i<=3) {fromPool = d.temperature;}
            }

            // From the solar
            if (d.sensor_id == '28-000005ea28bc')
            {
                d.temperature = d.temperature-0.875;
                if (i<=3) {fromSolar = d.temperature;}
            }
        });

// Scale the range of the data
        x.domain(d3.extent(data, function(d) { return d.date; }));
        y.domain([d3.min(data, function(d) { return d.temperature; })-1 ,
            d3.max(data, function(d) { return d.temperature; })
        ]);

// Nest the entries by sensor_id
        var dataNest = d3.nest()
                .key(function(d) {return d.sensor_id;})
                .entries(data);

        var color = d3.scale.category10();   // set the colour scale

        legendSpace = width/dataNest.length; // spacing for the legend

// Loop through each sensor_id / key
        dataNest.forEach(function(d,i) {

            svg.append("path")
                    .attr("class", "line")
                    .style("stroke", function() { // Add the colours dynamically
                        return d.color = color(d.key); })
                    .attr("id", 'tag'+d.key.replace(/\s+/g, '')) // assign ID
                    .attr("d", temperatureline(d.values));

            // Add the Legend
            svg.append("text")
                    .attr("x", (legendSpace/2)+i*legendSpace)  // space legend
                    .attr("y", height + (margin.bottom/2)+ 5)
                    .attr("class", "legend")    // style the legend
                    .style("fill", function() { // Add the colours dynamically
                        return d.color = color(d.key); })
                    .on("click", function(){
                        // Determine if current line is visible
                        var active   = d.active ? false : true,
                                newOpacity = active ? 0 : 1;
                        // Hide or show the elements based on the ID
                        d3.select("#tag"+d.key.replace(/\s+/g, ''))
                                .transition().duration(100)
                                .style("opacity", newOpacity);
                        // Update whether or not the elements are active
                        d.active = active;
                    })
                    .text(
                            function() {
                                if (d.key == '28-000005ea28bc') {
                                    return "Solar "+Math.round(fromSolar*10)/10;}
                                if (d.key == '28-0000062c46e3') {
                                    return "Pool "+Math.round(fromPool*10)/10;}
                                if (d.key == '28-0000062c49a2') {
                                    return "Return "+Math.round(toPool*10)/10;}
                                else {return d.key;}
                            });
        });


// Add the X Axis
        svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);

// Add the Y Axis
        svg.append("g")
                .attr("class", "y axis")
                .call(yAxis);

        var focus = svg.append("g")
                .style("display", "none");

        // append the x line
        focus.append("line")
                .attr("class", "x")
                .style("stroke", "blue")
                .style("stroke-dasharray", "3,3")
                .style("opacity", 0.5)
                .attr("y1", 0)
                .attr("y2", height);

        // append the y line
        focus.append("line")
                .attr("class", "y")
                .style("stroke", "blue")
                .style("stroke-dasharray", "3,3")
                .style("opacity", 0.5)
                .attr("x1", width)
                .attr("x2", width);

        // place the value at the intersection
        focus.append("text")
                .attr("class", "y1")
                .style("stroke", "white")
                .style("stroke-width", "3.5px")
                .style("opacity", 0.8)
                .attr("dx", 8)
                .attr("dy", "-.3em");
        focus.append("text")
                .attr("class", "y2")
                .attr("dx", 8)
                .attr("dy", "-.3em");

        // place the date at the intersection
        focus.append("text")
                .attr("class", "y3")
                .style("stroke", "white")
                .style("stroke-width", "3.5px")
                .style("opacity", 0.8)
                .attr("dx", 8)
                .attr("dy", "1em");
        focus.append("text")
                .attr("class", "y4")
                .attr("dx", 8)
                .attr("dy", "1em");

        // append the rectangle to capture mouse
        svg.append("rect")
                .attr("width", width)
                .attr("height", height)
                .style("fill", "none")
                .style("pointer-events", "all")
                .on("mouseover", function() { focus.style("display", null); })
                .on("mouseout", function() { focus.style("display", "none"); })
                .on("mousemove", mousemove);

        function mousemove() {
            var x0 = x.invert(d3.mouse(this)[0]),
                    y0 = d3.mouse(this)[1]
            y1 = Math.round(y.invert(y0)*10)/10,
                    date1 = d3.mouse(this)[0];

// console.log(x0);

            focus.select(".x")
                    .attr("transform", "translate(" + date1 + "," + (0) + ")")
                    .attr("y2", height );

            focus.select(".y")
                    .attr("transform",
                            "translate(" + width * -1 + "," + y0 + ")")
                    .attr("x2", width + width);

            focus.select("text.y1")
                    .attr("transform",
                            "translate(" + (date1 + 5) + "," + (y0) + ")")
                    .text(formatDate(x0));

            focus.select("text.y2")
                    .attr("transform",
                            "translate(" + (date1 + 5) + "," + (y0) + ")")
                    .text(formatDate(x0));
            focus.select("text.y3")
                    .attr("transform",
                            "translate(" + (date1 + 7) + "," + (y0 + 4) + ")")
                    .text(y1);

            focus.select("text.y4")
                    .attr("transform",
                            "translate(" + (date1 + 7) + "," + (y0 + 4) + ")")
                    .text(y1);
        }

    });

</script>
</body>