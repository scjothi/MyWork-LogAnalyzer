
 $("#pagn").click(function () {
        //var addressValue = $(this).attr("href");
        //alert('clicked' );
  });

/*
  $(function() {
    $( "#datepicker1" ).datepicker();
     $( "#datepicker2" ).datepicker();
  });
*/

$(document).ready( function() {
var date = new Date();

var day = date.getDate();
var month = date.getMonth() + 1;
var year = date.getFullYear();

if (month < 10) month = "0" + month;
if (day < 10) day = "0" + day;

var today = year + "-" + month + "-" + day;
	document.getElementById("datepicker1").value = year+'-'+month+'-'+day+'T'+'00:00';
	document.getElementById("datepicker2").value = year+'-'+month+'-'+day+'T'+'23:59';
});

  $(function() {
    $( "input[type=submit], a, button" )
      .button()
      .click(function( event ) {
        event.preventDefault();

        GenerateQuery();
      });
  });

String.prototype.insert = function (index, string) {
  if (index > 0)
    return this.substring(0, index) + string + this.substring(index, this.length);
  else
    return string + this;
};

function fetch(from, obj)
{
  //alert(obj);

  _start = from;
  //alert(_start);
  GenerateQuery();

}

function BuildDtQuery(from,to)
{
    var fdate= new Date();
    var tdate= new Date();

//2013-08-08T00:00
    var r = '';

    if(from != ''){
		var datetime = from.split('T'); from = datetime[0];
		from = from.split('-');
        //alert(from);
        fdate.setDate(from[2]); fdate.setMonth(from[1]-1);  fdate.setFullYear(from[0]);

		from = datetime[1];
		from = from.split(':');
		fdate.setHours(from[0]); fdate.setMinutes(from[1]);

        //alert(fdate.toISOString());
        r = dtstr.insert(dtstr.indexOf("TO"),fdate.toISOString()+' ');
          //alert(r);
    }
    else
    {
		var c=window.confirm("FROM DATE is empty or incomplete. Do you like to continue ?");
		if (c==true)
		  {
		  r = dtstr.insert(dtstr.indexOf("TO"),'* ');
		  }
		else
		  {
		  return '';
		  }

    }

    if(to != ''){
		var datetime = to.split('T'); to = datetime[0];
        to = to.split('-');
        //alert(to);
        tdate.setDate(to[2]); tdate.setMonth(to[1]-1);  tdate.setFullYear(to[0]);

		to = datetime[1];
		to = to.split(':');
		tdate.setHours(to[0]); tdate.setMinutes(to[1]);


        //alert(tdate.toISOString());
        r =  r.insert(r.indexOf("]"),' '+tdate.toISOString());
    }
    else
    {
	    var c=window.confirm("TO DATE is empty or incomplete. Do you like to continue ?");
		if (c==true)
		  {
		  r = r.insert(r.indexOf("]"),' *');
		  }
		else
		  {
		  return '';
		  }
    }


    //alert(r);


    return r;
}


 function getPages(recsize){

  //alert('called');

    var rpp = _rows; //recs per page
    var html = '';
    var start = 0;
    var end = 0;
    var applySt="";

     for(i=recsize;i>0;)
     {
         applySt="";
       if(end+rpp<=recsize)
       {
          i=i-rpp;
          start = end+1;
          end = end + rpp;

       }
       else
       {
          i=i-(recsize%rpp)
          start = end+1;
          end = end + (recsize%rpp);
         // html = html + start + '-'+ end + '|';
       }

        if(_start == start)
        {
            applySt="class='disabled'";
        }


        if(end == start)
        {
          html = html + '<a  href="#" '+applySt+' onclick="fetch('+start+',this)">' + start + '</a>|';
        }
        else
        {
          html = html + '<a  href="#" '+applySt+' onclick="fetch('+start+',this)">' + start + '-'+ end + '</a>|';  //href="#'+start+':'+end+'"
        }

     }



      //alert(html);
/*
      var pages = html.split("|");
      //alert(pages);

      var pageCount = pages.length;
      //alert(ll);
      var currPg = Math.floor(_start/recct);
      var startPg = currPg-5; startPg = (startPg<0 || pageCount<10) ? 0:startPg;
      //var en = cn+5; en = (en>ll) ? ll:en;

      alert(startPg+':'+currPg);
      html='';
      bbb:
      for(x=startPg;x<startPg+10;x++)
      {
        html = html + '|' + pages[x];
        if(x>=pageCount-1)
        {
           break outer;
        }
      }
*/

      var resultHtml = genPagination(html);
       _start=0;
      return resultHtml;

    }

function genPagination(html)
{

   //alert(html);
   var disPgCount=_pageCount; //no. of pages to display(should be EVEN)
      var rpp = _rows; //recs per page
      var pages = html.split("|");
      //alert(pages);

      var totPgCount = pages.length; //total number of pages
      //alert(ll);
      var currPg = Math.floor(_start/rpp);
      var startPg = currPg-(disPgCount/2);
      startPg = (startPg<0 || totPgCount<disPgCount) ? 0:startPg;

      //alert(startPg+':'+currPg);
      html='';

      for(x=startPg;x<startPg+disPgCount;x++)
      {
        html = html + '|' + pages[x];
        if(x>=totPgCount-1)
        {
          break;
        }
      }

      return html;
}

function appendParam(_query1, _query2)
  {
        if(_query1 != '' && _query2 != '')
        {
          _query1=_query1+'+AND+'+_query2;
        }
        else if(_query1 == '')
        {
         _query1 = _query2;
        }


        //alert(_query1);
        return _query1;
  }


  function GenerateQuery()
  {
        //alert('clicked');

        var srvr = document.getElementById("server").value;
        var inst  = document.getElementById("instance").value;
        var websvc  = document.getElementById("webservice").value;
        var lvl  = document.getElementById("level").value;
        var msg  = document.getElementById("message").value;
        var fromDt  = document.getElementById("datepicker1").value;
		//alert(fromDt);
        var toDt  = document.getElementById("datepicker2").value;
        var query = '';
        var dtq='';

		if(!checkChrome())
	    {
			return;
		}

        if(srvr != '*')
        {
          query=appendParam(query,'SERVER:'+srvr);
        }

        if(inst != '*')
        {
          query= appendParam(query,'INSTANCE:'+inst);
        }

        if(websvc != '*')
        {
         query= appendParam(query,'WEBSERVICE:'+websvc);
        }

        if(lvl != '*')
        {
         query= appendParam(query,'LEVEL:'+lvl);
        }

          var dtQuery = BuildDtQuery(fromDt,toDt);
				if(dtQuery == '')
				  {
					return;
				  }
        query=appendParam(query,dtQuery);

        if(msg != '*')
        {
         msg = msg.replace(/[\[\]()^{}@!+-/&|~*?:\\]/g, "\\$&");
         query= appendParam(query,'MESSAGE:'+encodeURIComponent(msg).replace(/%20/g, '+'));
        }



        if(query == '')
        {
         query='q=*:*';
        }
        else
        {
         query='q='+query;
        }

        query = query+'&fl=LINE,SERVER,INSTANCE,WEBSERVICE,TIMESTAMP,LEVEL,THREAD,CLASS,MESSAGE&wt=json&indent=true&start='+_start+'&rows='+_rows+'&sort=UTCTIMESTAMP desc,LINE desc';
        //,_version_

         document.getElementById("h123").value=query;//+'\n'+encodeURIComponent(query);

         ExecuteSearch(query);
  }




     function test(tab){
     //alert(tab);
        //$(tab).show();
    if ($(tab).is(":hidden")) {
        $(tab).slideDown("slow");
    } else {
        $(tab).hide();
    }


     }


function replacePipe(str, vnum)
{
if(str.indexOf("\t")!=-1){
 //alert(str);
 var result='';
 var array=str.split("\t");
 var causedByIndex = 0;
 var causedByCount = 0;
 var alpha = ["a", "b", "c", "d", "e", "f"];





 for (var i = 0; i < array.length; i++) {


     //first token
     if(i==0){
      result = result+array[i];
     }
     //tokens that are not first and those with no 'caused by'
     else if(i>0 && array[i].indexOf("»Caused by:") == -1){



         //5th token from the begin or 5th token after 'caused by'
         if(i==causedByIndex+4)
         {
          result = result+'<br></div><div id="'+alpha[causedByCount]+vnum+'" class="abc" ><span class="tab"/>'+array[i];
         }

         //if this is the last token
         else if(i==array.length-1){
           if(i<causedByIndex+4)
           {
            result = result+'<br><span class="tab"/>'+array[i]+'</div>';
           }
           else
           {
           result = result+'<br><span class="tab"/>'+array[i]+'</div>';
           }
         }
         else
         {
         result = result+'<br><span class="tab"/>'+array[i];
         }


     }

     //token that has a 'caused by'
     else if(array[i].indexOf("»Caused by:") != -1){
       //if this 'caused by' occured within 5th line from the begin or from the previous 'caused by'
       if(i<causedByIndex+4)
       {
        //result = result+'<br><span class="tab"/>'+array[i].replace("\tCaused by:", "<br>Caused by:");
        result = result+'<br><span class="tab"/>'+array[i].replace("\t", "<br>");
        result = result.replace("»Caused by:", "<br>Caused by:");
       }
       else
       {
        //result = result+'<br><span class="tab"/>'+array[i].replace("\tCaused by:", "</div><br>Caused by:");
        result = result+'<br><span class="tab"/>'+array[i].replace("\t", "</div><br>");
        result = result.replace("»Caused by:", "</div><br>Caused by:");
       }
      causedByIndex = i;
      causedByCount++;
     }


/*

     if(i==causedByIndex+4){
      result = result+'<div id="a'+vnum+'" class="abc" ><span class="tab"/>'+array[i];
     }
     else if(i==array.length-1){
      result = result+'<br><span class="tab"/>'+array[i]+'</div>';
     }
     else if(i==0){
      result = result+array[i];
     }
     else if(i>0 && array[i].indexOf("Caused by:") == -1){

      result = result+'<br><span class="tab"/>'+array[i];
     }
     else if(i>0 && array[i].indexOf("Caused by:") != -1){
      result = result+'</div><br>'+array[i];
      causedByIndex = i;
     }

*/
 }



 //alert(result);
 //document.getElementById("h123").value=result;
  return result;
 }
// alert(result)
return str;
 }

        // JSON to CSV Converter
        function ConvertToCSV(objArray) {
        //alert(objArray);
            var array = typeof objArray != 'object' ? JSON.parse(objArray) : objArray;
            var str = '';

            for (var i = 0; i < array.length; i++) {
                var line = '';
                for (var index in array[i]) {
                    if (line != '') line += '|'

                    line += array[i][index];
                    line = replacePipe(line, i); // line.replace(/¦/g,'<br><span class="tab"/>');
                    line = line.replace(/\t/g,'<br>');

                }


                var hrf = "javascript:test('#a" + i + "')";
                //alert(hrf);
                str += '<tr><td valign="top">'+i+'</td><td>'+   line + '<br><a href="'+hrf+'">Show All</a>'   +   '</td></tr>';
            }


            return "<table id='page1' width='100%' border='0'>"+str+"</table>";
        }


     function ExecuteSearch(url)
     {

     //alert('ExecuteSearch');

     url = _uri+url;

    //$.getJSON("http://localhost:7070/solr-example/collection1/select?q=LEVEL%3AERROR&rows=1500&fl=id%2CSERVER%2CINSTANCE%2CWEBSERVICE%2CTIMESTAMP%2CLEVEL%2CTHREAD%2CCLASS%2CMESSAGE%2C_version_&wt=json&indent=true", function(rec) {

   $('#spinner').show();

    $.getJSON(url, function(rec) {

    //alert(rec);



    $.each(rec, function(key, val) {
    if(key == "response")
    {
      numFound = val["numFound"];
      //INCREASING THE RECORDS PER PAGE LENGTH IF MORE THAN 1 MILLION MATCHES FOUND
      /*
     if(numFound > 1e6){
      _rows = 10000;
     }
     */

     //$('div.results').html('<tr><td width="100%"  bgcolor="white" colspan="5" >Number of Records: '+numFound +getPages(numFound)+ '</td></tr>');
	  $('div.results').html('Number of Records: '+numFound); // +getPages(numFound));
	   $('div.pages').html(getPages(numFound));

     jsonObject = val["docs"];
      $('#csv').html(ConvertToCSV(jsonObject));
      $('tr:nth-child(even)').css("background", "#cccccc");
     }  //alert(jsonObject);

	  $('#spinner').hide();
   
  });
});

            // Create Object
            var items;
            var items1 = [
                    {
        "id": "1424066",
        "SERVER": "vaqesb01",
        "INSTANCE": "QA-A",
        "WEBSERVICE": "WebPortalWebServices-v798",
        "TIMESTAMP": "2013-05-31 10:51:49,915",
        "UTCTIMESTAMP": "2013-05-31T14:51:49.915Z",
        "LEVEL": "ERROR",
        "THREAD": "http--10.100.112.162-8080-18",
        "CLASS": "com.agpcorp.webportalwebservices.helpers.IdAndPasswordHelper.validateUsername(IdAndPasswordHelper.java:1025)",
        "MESSAGE": [
          "[WPWS] validateUser: MDamgpcaid01 was not found in the system"
        ],
        "_version_": 1438944177620516900
      }];

            // Convert Object to JSON
         //  var jsonObject; // = JSON.stringify(items);

            // Display JSON
           // $('#json').text(jsonObject);

            // Convert JSON to CSV & Display CSV
           // $('#csv').html(ConvertToCSV(jsonObject));
        } //);


function exportTableToCSV($table, filename) {

	if($table.length == 0)
	{
		   alert('No data found to export.');
		   return;
	}

        var $rows = $table.find('tr:has(td)'),

            // Temporary delimiter characters unlikely to be typed by keyboard
            // This is to avoid accidentally splitting the actual contents
            tmpColDelim = String.fromCharCode(11), // vertical tab character
            tmpRowDelim = String.fromCharCode(0), // null character

            // actual delimiter characters for CSV format
            colDelim = '","',
            rowDelim = '"\r\n"',

            // Grab text from table into CSV formatted string
            csv = '"' + $rows.map(function (i, row) {
                var $row = $(row),
                    $cols = $row.find('td');

                return $cols.map(function (j, col) {
                    var $col = $(col),
                        text = $col.text();

                    text = text.replace('"', '""'); // escape double quotes

					text= text.replace('|','","');
					text=text.replace('|','","');
					text=text.replace('|','","');
					text=text.replace('|','","');
					text=text.replace('|','","');
					text=text.replace('|','","');
					text=text.replace('|','","');
					return text.replace('|','","');

                }).get().join(tmpColDelim);

            }).get().join(tmpRowDelim)
                .split(tmpRowDelim).join(rowDelim)
                .split(tmpColDelim).join(colDelim) + '"',

            // Data URI
            csvData = 'data:application/csv;charset=utf-8,' + encodeURIComponent(csv);


		//var encodedUri = encodeURI(csvContent);
var link = document.createElement("a");
link.setAttribute("href", csvData);
link.setAttribute("download", "prod_esb_logs.csv");

link.click(); // This will download the data file named "my_data.csv".

}