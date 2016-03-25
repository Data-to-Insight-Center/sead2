$(function() {

	
	var request = $.ajax({
		url: "./../log/dataone",
		type: "get",
		contentType : 'application/json',
		success: function (result){
              var data=JSON.stringify(result);
              Morris.Line({
                  element: 'morris-area-chart',
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
	
/*	
    Morris.Area({
        element: 'morris-area-chart',
        data: data2,
        xkey: 'period',
        ykeys: ['iphone', 'ipad', 'itouch'],
        labels: ['iPhone', 'iPad', 'iPod Touch'],
        pointSize: 2,
        hideHover: 'auto',
        resize: true
    });*/

/*    Morris.Donut({
        element: 'morris-donut-chart',
        data: [{
            label: "Download Sales",
            value: 12
        }, {
            label: "In-Store Sales",
            value: 30
        }, {
            label: "Mail-Order Sales",
            value: 20
        }],
        resize: true
    });

    Morris.Bar({
        element: 'morris-bar-chart',
        data: [{
            y: '2006',
            a: 100,
            b: 90
        }, {
            y: '2007',
            a: 75,
            b: 65
        }, {
            y: '2008',
            a: 50,
            b: 40
        }, {
            y: '2009',
            a: 75,
            b: 65
        }, {
            y: '2010',
            a: 50,
            b: 40
        }, {
            y: '2011',
            a: 75,
            b: 65
        }, {
            y: '2012',
            a: 100,
            b: 90
        }],
        xkey: 'y',
        ykeys: ['a', 'b'],
        labels: ['Series A', 'Series B'],
        hideHover: 'auto',
        resize: true
    });
*/
});

/*function sendToServer(){
		var request = $.ajax({
						url: "http://localhost:8180/sead-mon/log/data",
						type: "get",
						contentType : 'application/json',
						success: function (result){
			                  var data2=JSON.stringify(result);;
			              }
						});
}*/
