/*
This is the instructions on how to build your own social share toolbar using jQuery and CSS3. The toolbar should be visible at the bottom right corner of your browser. If you hover over the toolbar it will slide up, click the minimize button it will all but disappear, click one of the icons and you will be taken to either the login page or the share page of that social site.

http://www.jquery4u.com/tutorials/jquery-socialize-sharing-tool

*/
(function ($) {
    $(document).ready(function () {
        var url = window.location.href;
        var host = window.location.hostname;
        var title = $('title').text();
        title = escape(title); //clean up unusual characters

        var twit = 'http://twitter.com/home?status=' + title + '%20' + url;
        var facebook = 'http://www.facebook.com/sharer.php?u=' + url
        var digg = 'http://digg.com/submit?phase=2&url=' + url + '&amp;title=' + title;
        var stumbleupon = 'http://stumbleupon.com/submit?url=' + url + '&amp;title=' + title;
        var buzz = 'http://www.google.com/reader/link?url=' + url + '&amp;title=' + title + '&amp;srcURL=' + host;
        var delicious = 'http://del.icio.us/post?url=' + url + '&amp;title=' + title;

        //var tbar = '<div id="socializethis" ><span>Tools<br /><a href="#min" id="minimize" title="Minimize"> <img src="images/minimize.png" /> </a></span><div id="sicons">';
        //tbar += '<a href="'+twit+'" id="twit" title="Share on twitter"><img src="images/twitter.png"  alt="Share on Twitter" width="32" height="32" /></a>';
        //tbar += '<a href="'+facebook+'" id="facebook" title="Share on Facebook"><img src="images/facebook.png"  alt="Share on facebook" width="32" height="32" /></a>';
        //tbar += '<a href="'+digg+'" id="digg" title="Share on Digg"><img src="images/digg.png"  alt="Share on Digg" width="32" height="32" /></a>';
        //tbar += '<a href="'+stumbleupon+'" id="stumbleupon" title="Share on Stumbleupon"><img src="images/stumbleupon.png"  alt="Share on Stumbleupon" width="32" height="32" /></a>';
        //tbar += '<a href="' + buzz + '" id="buzz" title="Share on Buzz"><img id="help" src="images/help.png"  alt="Share on Buzz" width="55" height="55" /></a>';
        //tbar += '<a href="' + delicious + '" id="delicious" title="Download current page as CSV"><img id="csv" src="images/csv_export.png"  alt="Download current page as CSV" width="55" height="55" /></a>';

        //tbar += '</div></div>';
/*
        var tbar = '';

        tbar += '<div id="socializethis" style="position:absolute; bottom:10px; left:600px;" ><div id="tools"></div>';
        tbar += '<ul id="navlist">';
        
        tbar += '<li><a href="' + buzz + '" id="ahelp" title="Share on Buzz"><img id="help" src="images/help.png"  alt="Share on Buzz" width="35" height="35" /></a></li>';
        tbar += '<li><a href="' + delicious + '" id="acsv" title="Download current page as CSV"><img id="csv" src="images/csv3.png"  alt="Download current page as CSV" width="35" height="35" /></a></li>';
        tbar += '<li><a href="' + delicious + '" id="alink" title="Download current page as CSV"><img id="link" src="images/link.png"  alt="Download current page as CSV" width="35" height="35" /></a></li>';
		tbar += '<li><a href="#min" id="minimize" title="Minimize"> <img src="images/mini3.png" width="35" height="35" /> </a></li>';
        tbar += '</ul>';
        tbar += '</div>';
*/

var tbar = '';
tbar += '<div id="socializethis" ><table border="0" width="245" height="30" bgcolor="grey" cellpadding="0" cellspacing="0">';
tbar += '<tr>';
tbar += '<td id="navlist" align="center" ></td>';
tbar += '<td id="navlist" align="center" ></td>';
tbar += '<td id="navlist" align="right" ><img id="clickme" src="images/minimize.png" height="11"/></td>';
tbar += '</tr>';
tbar += '<tr>';
tbar += '<td id="navlist" align="center"   bgcolor="grey"><img id="csv1" src="images/csv3.png"  alt="Share on Buzz" width="35" height="35" /></td>';
tbar += '<td id="navlist" align="center"  bgcolor="grey"><img id="help" src="images/help.png"  alt="Share on Buzz" width="35" height="35" /></td>';
tbar += '<td id="navlist" align="center"  bgcolor="grey"><img id="link" src="images/link.png"  alt="Share on Buzz" width="35" height="35" /></td>';
tbar += '</tr>';
tbar += '</table></div>';

        // Add the share tool bar.
        $('body').append(tbar);
        $('#socializethis').css({
           // height: '55px',
            //width: '250px',
			 height:10, width:250,
            opacity: .7,
            bottom: 5,
            left: 560		
        });

        $('#csv1').css({
            opacity: .5
        });
        $('#help').css({
            opacity: .5
        });
        $('#link').css({
            opacity: .5
        });
var toolTab = true;
$('#clickme').click(function() {
	if(toolTab == true){
   $('#socializethis').animate({
                //height: '55px',
				 height:55, width:250,
                //width: '250px',
                opacity: 1
            }, 300);  toolTab = false;
	}
	else
	{
		   $('#socializethis').animate({
                //height: '55px',
				 height:10, width:250,
                //width: '250px',
                opacity: 1
            }, 300);  toolTab = true;
	}




});


$('#csv1').bind('click', function () {
var obbj = document.getElementById('page1');
exportTableToCSV($(obbj),'a.csv');
});


$('#help').bind('click', function () {
window.open ("LogAnalyzerFAQ.htm","mywindow","menubar=0,resizable=1,width=500,height=680");
});

 $('#socializethis').bind('mouseenter', function () {
            $('#socializethis').css({
                opacity: 1
            });
        });

        // hover.
		/*
        $('#socializethis').bind('mouseenter', function () {
            $(this).animate({
                //height: '55px',
				 height:55, width:250,
                //width: '250px',
                opacity: 1
            }, 300);
            $('#socializethis img').css('display', 'inline');
            $('#tools').html('');
        });
*/

        $('#csv1').bind('mouseenter', function () {
            $('#csv1').css({
                opacity: 1
            });
			$('#csv1').css({
                cursor:'hand'
            });
        });

        $('#help').bind('mouseenter', function () {
            $('#help').css({
                opacity: 1
            });
			$('#help').css({
                cursor:'hand'
            });
        });

        $('#link').bind('mouseenter', function () {
            $('#link').css({
                opacity: .5
            });
        });

        $('#help').bind('mouseleave', function () {
            $('#help').css({
                opacity: .5
            });
        });

        $('#csv1').bind('mouseleave', function () {
            $('#csv1').css({
                opacity: .5
            });
        });

        $('#link').bind('mouseleave', function () {
            $('#link').css({
                opacity: .5
            });
        });

        //leave
        $('#socializethis').bind('mouseleave', function () {
            $(this).animate({
                opacity: .7
            }, 300);
        });
        // Click minimize
        $('#socializethis #minimize').click(function () {
            minshare();
            // $.cookie('minshare', '1');  
			 $('#tools').html('<center><b>&#8743;</b></center>');
        });

        // if($.cookie('minshare') == 1){
        //  minshare();
        // }  

        function minshare() {
            $('#socializethis').animate({
                //height: '15px',
                //width: '250px',
				height:10, width:250,
                opacity: .7,
                bottom: 5,
            left: 560
            }, 300);
            $('#socializethis img').css('display', 'none');
           
            return false;
        }
    });
})(jQuery);



//$(document).ready(function () {

  
