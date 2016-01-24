<%--
  Created by IntelliJ IDEA.
  User: julian
  Date: 12/6/15
  Time: 6:34 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <script src="//code.jquery.com/jquery-1.11.3.min.js"></script>
    <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
    <script src="assets/js/moment-with-locales.js"></script>
    <script src="assets/js/jquery.jscroll.js"></script>
    <script type="text/javascript">

        Number.prototype.toFixedDown = function(digits) {
            var re = new RegExp("(\\d+\\.\\d{" + digits + "})(\\d)"),
                    m = this.toString().match(re);
            return m ? parseFloat(m[1]) : this.valueOf();
        };




    </script>

    <script type="text/javascript">
        function getParameterByName(name) {
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
                    results = regex.exec(location.search);
            return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
        };

    </script>

    <script>
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
                rest_url = "/rest/analytics/top/"+top+"/by/"+sort_by+"/date/"+date+"/user/"+username;
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
                    //var p_start = analytic["analytics"]["period-coverage-end-month"].toFixed(1);
                    var period_posts_count = analytic["analytics"]["engagement-analytics-meta"]["period-posts-count"].toFixed(1);
                    var earliest_in_period = analytic["analytics"]["engagement-analytics-meta"]["earliest-in-period"];
                    var latest_in_period = analytic["analytics"]["engagement-analytics-meta"]["latest-in-period"];
                    var period_name = analytic["analytics"]["engagement-analytics-meta"]["period"];
                    var period_days = analytic["analytics"]["engagement-analytics-meta"]["period-days"].toFixed(1);
                    var posts_per_day = analytic["analytics"]["engagement-analytics-meta"]["posts-per-day"].toFixed(1);

                    var image_content = "<div class='row'>";
                    var col_count = 0;
                    var status_json = analytic["analytics"]["status-json"];
                    var item_count = 1;
                    status_json.forEach(function(status) {
                        image_content += "<div class='col-sm-4 center-block' style='background-color:black; padding-left:0px; padding-right:0px; padding-bottom: 0px; padding-top: 3px'><img src='"+status["images"]["low_resolution"]["url"]+"' class='img-responsive'  /></div>";
                        //image_content+="<div class='col-sm-4 center block'>"+col_count+"</div>";
                        col_count++;
                        if(col_count % 3 == 0) {
                            //x++;
                            if(item_count == status_json.length) {
                                image_content += "</div>"
                            } else {
                                image_content += "</div><div class='row'>";
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
                    var content = ""
                    content += "<div style='margin-top: 10px'><table class='table table-striped'><tbody>";
                    content += "<tr><td>period-posts-count</td><td>"+period_posts_count+"</td>";
                    content += "<td>period-days</td><td>"+period_days+"</td>";
                    content += "<td>posts-per-day</td><td>"+posts_per_day+"</td></tr>";
                    content += "<tr><td>Period</td><td>"+period_name+"</td>";
                    content += "<td>avg-likes</td><td>"+avg_likes+"</td>"
                    content += "<td>min-likes</td><td>"+min_likes+"</td></tr>";
                    content += "<tr><td>max-likes</td><td>"+max_likes+"</td>";
                    content += "<td>avg-comments</td><td>"+avg_comm+"</td>";
                    content += "<td>min-comments</td><td>"+min_comm+"</td></tr>";
                    content += "<tr><td>max-comments</td><td>"+max_comm+"</td>";
                    content += "<td>avg-mentions</td><td>"+avg_ment+"</td>";
                    content += "<td>avg-tags</td><td>"+avg_tags+"</td></tr>";
                    content += "<tr><td>Earliest Post in Period</td><td>"+earliest_in_period+"</td>";
                    content += "<td>Latest Post in Period</td><td>"+latest_in_period+"</td><td></td><td></td></tr></tbody></table></div>";
                    //content += "</div>";


                    // var foo = document.createElement("div");
                    //$("#something").append('<div class=\'row\'>WTF<div class=\'col-sm-4\'>');
                    $("#something").append("<div class='panel panel-primary'><div class='panel-heading'><h3 class='panel-title'>#<b>"+(count++)+"</b> @"+username+"</h3></div></div><div class='panel-body' style='background-color:white'>"+image_content+"<div>"+content+"</div></div></div>");
                    $("#something").append("")
                    //$("#something").append("<div class='col-lg-12'>Hello Tuna!</div></div>");
                    //$("#something").append("<div class='col-lg-12'>"+content+"</div></div></div>");

                    //$("#something").append("#<b>"+(count++)+"</b> @"+username+"<div class='panel-body'><div class='col-lg-14'>"+image_content+"</div></div>");

                    //$("#something").append("</div></div>");
                });
            });
            //$("body").append("</div>");

        });
    </script>
    <script>

    </script>


    <meta charset="utf-8">
    <title>Tops</title>
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">

    <!-- Optional theme -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css" integrity="sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r" crossorigin="anonymous">

    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>

</head>
<body>
<div class="container theme-showcase" role="main">
    <div id="title"></div>
    <div class="row">
        <div id="something"></div>
    </div>
    <%--<div id="username">Jello</div>--%>
    <%--<div id="snapshot-date">snapshot-date</div>--%>
    <%--<div id="snapshot-time">snapshot-time</div>--%>
    <%--<div id="snapshot-period-coverage">snapshot-period-coverage</div>--%>
    <%--<div id="period-posts-count">period-posts-count</div>--%>
    <%--<div id="period">period</div>--%>
    <%--<script src="assets/js/jquery.js"></script>--%>
    <%--<script src="assets/js/select2.js"></script>--%>
    <%--<script src="assets/js/bootstrap.js"></script>--%>

    <div class="page-header">
        <h1>Panels</h1>
    </div>
    <div class="row">
        <div class="col-sm-3">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">Panel title</h3>
                </div>
                <div class="panel-body">
                    <div id="infinite-scroll"><input type="submit" value="A submit button">
                        <br/>
                        <a href="#">An anchor</a>
                    </div>
                </div>
            </div>
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">Panel title</h3>
                </div>
                <div class="panel-body">
                    Panel content
                </div>
            </div>
        </div><!-- /.col-sm-4 -->
        <div class="col-sm-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title">Panel title</h3>
                </div>
                <div class="panel-body">
                    Panel content
                </div>
            </div>
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title">Panel title</h3>
                </div>
                <div class="panel-body">
                    Panel content
                </div>
            </div>
        </div><!-- /.col-sm-4 -->
        <div class="col-sm-4">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <h3 class="panel-title">Panel title</h3>
                </div>
                <div class="panel-body">
                    Panel content
                </div>
            </div>
            <div class="panel panel-danger">
                <div class="panel-heading">
                    <h3 class="panel-title">Panel title</h3>
                </div>
                <div class="panel-body">
                    Panel content
                </div>
            </div>
        </div><!-- /.col-sm-4 -->
    </div>
</div>
</body>
</html>
