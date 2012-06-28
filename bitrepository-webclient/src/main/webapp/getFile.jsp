<%--
  #%L
  Bitrepository Webclient
  %%
  Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as 
  published by the Free Software Foundation, either version 2.1 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.
  
  You should have received a copy of the GNU General Lesser Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-2.1.html>.
  #L%
  --%>

<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<html>
<link type="text/css" href="css/ui-lightness/jquery-ui-1.8.16.custom.css" rel="Stylesheet" />	
<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="defaultText.js"></script>


	<script>
	    $(function() {
		    $("#getFileForm").buttonset();
	    });
    </script>
	
	<div class=ui-widget id=getFileTab>
		<form id="getFileForm" action="javascript:submit()">
	        <p><b>Get file</b></p>
	        <table border="0">
	            <tr>
	                <td>Filename:</td>
	                <td><input class="defaultText" title="Filename" id="getFilename" type="text"/></td>
	            </tr>
	            <tr>
	                <td>Fileaddress:</td>
	                <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
	                <td> <input type="text" class="inputURL" name="fileaddr" id="getFileaddr" value="<%= su.getDefaultHttpServerUrl() %>"/></td>
	            </tr>
	        </table> 
	        <input type="submit" value="Get file"/>
	    </form>
	</div>
	<div id="messagediv"></div>
	<div id="completedFilesDiv"></div>

    <script>
        var auto_completedfiles = setInterval(
        function() {
            $('#completedFilesDiv').load('repo/reposervice/getfile/getCompletedFiles/').fadeIn("slow");
            }, 2500);
    </script> 

    <script>
        $("#getFileForm").submit(function() {
            var fileName = $("#getFilename").val();
            var fileAddr = $("#getFileaddr").val();
    
    
            if (fileName == "") {
                //$('#messagediv').html("<p2>Invalid filename!</p2>").show().fadeOut(5000);
                return false;
            }
            if (fileAddr == "") {
                //$('#messagediv').html("<p2>Invalid address!</p2>").show().fadeOut(5000);
                return false;
            }
            var command = "repo/reposervice/getfile/?fileID=" + fileName + "&url=" + fileAddr;
        
            $('#messagediv').load(command, function(response, status, xhr) {
                if (status == "error") {
                    $("#messagediv").html(response);
                }
            }).show();
            
            return true;
        });
    </script>
    
    <script>
        function submit() { return ; }
    </script>  
</html>