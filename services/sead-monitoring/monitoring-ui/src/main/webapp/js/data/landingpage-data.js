$(document).ready(function (){

   $.ajax({
        url: "./../log/landingPage",
        type: "get",
        contentType : 'application/json',
        success: function (result){
              var data=JSON.stringify(result);
              Morris.Line({
                  element: 'morris-landingpage-access-line-chart',
                  data: result,
                  xkey: 'date',
                  ykeys: ['count'],
                  labels: ['Count'],
                  pointSize: 3,
                  hideHover: 'auto',
                  resize: true,
                  pointFillColors:['#ffffff'],
                  pointStrokeColors: ['black'],
                  lineColors:['blue']
              });
        }
   });
});