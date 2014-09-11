package atri.aws.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Servlet implementation class UploadFile
 */
public class UploadFile extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static String bucketName = "pavanatribucket";
	
	private static String keyName;
	File tempFile;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadFile() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String convId = null;
		try {
//			ServletContext servletContext = this.getServletConfig().getServletContext();
			
			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			for (FileItem item : items) {
				if (!item.isFormField()) {
					// Process form file field (input type="file").
					convId= genConvId();
					System.out.println("Generated convId:"+convId);
					if (FilenameUtils.getName(item.getName()).endsWith(".flv"))
						keyName = convId+"-1.flv";
					else
						keyName = convId+"-1.mp4";
					System.out.println("New broadcast Msg keyname:"+keyName);
					InputStream is = item.getInputStream();
					// ... (do your job here)
					tempFile = new File(keyName);
					OutputStream outputStream = new FileOutputStream(tempFile);

					int read = 0;
					byte[] bytes = new byte[1024];

					while ((read = is.read(bytes)) != -1) {
						outputStream.write(bytes, 0, read);
					}
				}
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
        
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/twittTubeHome.jsp");
        dispatcher.forward(request, response);
		
	}
	
	public static String genConvId()
	{
	    Random randomGenerator = new Random();
	    return Integer.toString(randomGenerator.nextInt(Integer.MAX_VALUE)+1);
	}

}
