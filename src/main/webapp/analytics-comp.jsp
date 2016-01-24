<%--
  Created by IntelliJ IDEA.
  User: julian
  Date: 1/10/16
  Time: 11:31 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<meta charset="utf-8">
<body>
<script src="//d3js.org/d3.v3.min.js"></script>
<script>

    var data = [1, 10, 2, 3, 5, 8, 13, 21];

    var width = 960,
            height = 500,
            radius = height / 2 - 10;

    var arc = d3.svg.arc()
            .innerRadius(radius - radius/2)
            .outerRadius(radius);

    var pie = d3.layout.pie()
            .padAngle(.01);
            //.startAngle(3.1415/2)

    var color = d3.scale.category10();

    var svg = d3.select("body").append("svg")
            .attr("width", width)
            .attr("height", height)
            .append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

    svg.selectAll("path")
            .data(pie(data))
            .enter().append("path")
            .style("fill", function(d, i) { return color(i); })
            .attr("d", arc);

</script>
