package atri.aws.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Servlet implementation class ConversationList
 */
public class ConversationList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String bucketName     = "pavanatribucket";
	private static String keyName;
	File tempFile;

	TwittTubeDAL obj = new TwittTubeDAL();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ConversationList() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String fileName = request.getParameter("keyName");
		System.out.println("Broadcast keyname whose Conversation to be displayed:"+fileName);
		String convId = fileName.substring(0, fileName.indexOf("-"));
		ArrayList<String> convList = obj.selectConversations(convId);
		request.setAttribute("convList", convList);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/listConversations.jsp");
		dispatcher.forward(request, response);		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String convId = null;
		int convMsgNo = 2;
		String lastVideoFile = null;
		String uploadedFile = null;
		InputStream is = null;
		try {

			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			for (FileItem item : items) {
				if(item.isFormField())
				{
					System.out.println("Item type:"+item.getFieldName()+"Item value:"+item.getString()+"Name:"+item.getName());
					if (item.getFieldName().equalsIgnoreCase("lastVideoFile"))
					{
						System.out.println("**************here***********8");
						lastVideoFile = item.getString();
					}
				}
				else {
					uploadedFile = FilenameUtils.getName(item.getName());
					is = item.getInputStream();
				}
			}

			System.out.println("Last Video FIle:"+lastVideoFile);
			convId = lastVideoFile.substring(0, lastVideoFile.indexOf("-"));
			//Parsing convMsgNo from lastVideoFile name. e.g., in lastVideoFile name 34573468-23.flv, 23 is convMsgNo
			convMsgNo = Integer.parseInt(lastVideoFile.substring(lastVideoFile.indexOf("-")+1, lastVideoFile.indexOf(".")));
			keyName = convId + "-" + (++convMsgNo);
			if (uploadedFile.endsWith(".flv"))
				keyName = keyName + ".flv";
			else
				keyName = keyName + ".mp4";
			System.out.println("New Msg in conversation:"+keyName);
			// ... (do your job here)
			tempFile = new File(keyName);
			OutputStream outputStream = new FileOutputStream(tempFile);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (FileUploadException e) {
			throw new ServletException("Cannot parse multipart request.", e);
		}

		/////////////////////////////////////////////////
		TwittTubeDAL objTwitt = new TwittTubeDAL();
		objTwitt.insertRecord(convId, 1, keyName);
		///////////////////////////////////////////////////

		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
		AmazonS3 s3client = new AmazonS3Client(credentialsProvider);
		try {
			System.out.println("Uploading a new object to S3 from a file\n");

			AccessControlList acl = new AccessControlList();
			acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);             	
			s3client.putObject(new PutObjectRequest(bucketName, keyName, tempFile).withAccessControlList(acl));

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which " +
					"means your request made it " +
					"to Amazon S3, but was rejected with an error response" +
					" for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which " +
					"means the client encountered " +
					"an internal error while trying to " +
					"communicate with S3, " +
					"such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
		request.setAttribute("convList", obj.selectConversations(convId));
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/listConversations.jsp");
		dispatcher.forward(request, response);
	}


}
