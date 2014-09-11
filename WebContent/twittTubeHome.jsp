<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="com.amazonaws.*"%>
<%@ page import="com.amazonaws.auth.*"%>
<%@ page import="com.amazonaws.services.ec2.*"%>
<%@ page import="com.amazonaws.services.ec2.model.*"%>
<%@ page import="com.amazonaws.services.s3.*"%>
<%@ page import="com.amazonaws.services.s3.model.*"%>
<%@ page import="com.amazonaws.services.dynamodbv2.*"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.*"%>

<%!// Share the client objects across threads to
	// avoid creating new clients for each web request
	private AmazonEC2 ec2;
	private AmazonS3 s3;
	private AmazonDynamoDB dynamo;
	private static String bucketName = "pavanatribucket";
    ObjectListing objectListing;
    ListObjectsRequest listObjectsRequest;
%>

<%
    /*
     * AWS Elastic Beanstalk checks your application's health by periodically
     * sending an HTTP HEAD request to a resource in your application. By
     * default, this is the root or default resource in your application,
     * but can be configured for each environment.
     *
     * Here, we report success as long as the app server is up, but skip
     * generating the whole page since this is a HEAD request only. You
     * can employ more sophisticated health checks in your application.
     */
    if (request.getMethod().equals("HEAD")) return;
%>

<%
    if (ec2 == null) {
        AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        //ec2    = new AmazonEC2Client(credentialsProvider);
        s3     = new AmazonS3Client(credentialsProvider);
        //dynamo = new AmazonDynamoDBClient(credentialsProvider);
        listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
    }
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Hello AWS Web World!</title>
<link rel="stylesheet" href="styles/styles.css" type="text/css"
	media="screen">
	<script type="text/javascript">
	function getConvList(keyName)
	{
		document.location.href="/ConversationList?keyName="+keyName;
	}
	</script>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script type='text/javascript' src='https://d1mhrncrfklq8e.cloudfront.net/jwplayer.js'></script>
<script type="text/javascript">

$(document).ready(function(e) {
	
    if(navigator.userAgent.match(/Android/i)
      || navigator.userAgent.match(/webOS/i)
      || navigator.userAgent.match(/iPhone/i)
      || navigator.userAgent.match(/iPad/i)
      || navigator.userAgent.match(/iPod/i)
      || navigator.userAgent.match(/BlackBerry/i)
      || navigator.userAgent.match(/Windows Phone/i)) {

     //write code for your mobile clients here.
alert("you are on mobile");

}
    else
{
	alert(navigator.userAgent);
}
});
</script>


</head>
<body>
	<div id="content" class="container">
		<div class="section grid grid5 s3">
			<h2>Upload Broadcast Message</h2>
			Select a file to upload: <br />
			<form action="UploadFile" method="post" enctype="multipart/form-data">
			<input type="file" name="fileurl" size="50" />
			<br />
			<input type="submit" value="Upload File" />
			</form>
	    </div>
	    
	    <table>
			    <% do { %>
			        <% objectListing = s3.listObjects(listObjectsRequest);%>
			        <% for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) { %>
			    	<tr>
			    		<td>
							<% if (objectSummary.getKey().endsWith("-1.flv") || objectSummary.getKey().endsWith("-1.mp4")) { %>
								<% String userAgent = request.getHeader("user-agent"); %>
								<% if (userAgent.contains("Android") || userAgent.contains("iPhone") || userAgent.contains("iPad") 
									|| userAgent.contains("BlackBerry") || userAgent.contains("Windows Phone")) {%>
																			
										 <a href="http://d1mhrncrfklq8e.cloudfront.net/<%=objectSummary.getKey() %>">
										 	<img alt="" src="http://d1mhrncrfklq8e.cloudfront.net/jwplayer.gif" />
										 </a>
										 <%=objectSummary.getKey() %>
									<% } else { %>
									<div align="center"  id="<%=objectSummary.getKey() %>"></div>
									<script type="text/javascript">									
										jwplayer('<%=objectSummary.getKey()%>').setup(
												{
													file : "rtmp://s1mdxmsqflz815.cloudfront.net/cfx/st/<%=objectSummary.getKey() %>",
													height : "240",
													width : "320"
												});	
									</script>													
									<% } %>	
					    </td>
					    <td><input type="button" value="Reply" name="<%= objectSummary.getKey() %>" onclick="getConvList('<%= objectSummary.getKey()%>');" ></td>
				    </tr>
							<% } %>
			        <% } %>
			        <% listObjectsRequest.setMarker(objectListing.getNextMarker()); %>
			    <% } while (objectListing.isTruncated());%>
	    </table>
	    
	</div>




</body>
</html>