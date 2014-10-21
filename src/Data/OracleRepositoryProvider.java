package Data;


import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import Business.Issue;
import oracle.jdbc.OracleTypes;
import Presentation.IRepositoryProvider;

/**
 * Encapsulates create/read/update/delete operations to Oracle database
 * @author matthewsladescu
 *
 */
public class OracleRepositoryProvider implements IRepositoryProvider {
	   // connection parameters - ENTER YOUR LOGIN AND PASSWORD HERE
    private final String userid   = "xzha4611";
    private final String passwd   = "xzha4611";
    private final String database = "oracle12.it.usyd.edu.au:1521:COMP5138";
    // instance variable for the database connection   
    private Connection conn = null; 

    public OracleRepositoryProvider(){
        try 
        {   
            /* load Oracle's JDBC driver */
            Class.forName ("oracle.jdbc.driver.OracleDriver");
        }
        catch (ClassNotFoundException no_class_ex) 
        {  
            /* error handling when no JDBC class is found */
            System.out.println(no_class_ex);
        }
    }
    
   
    
    
    
    
    /**
     * Establishes a connection to the Oracle database.
     * The connection parameters are read from the instance variables above
     * (userid, passwd, and database).
     * @returns  true   on success and then the instance variable 'conn' 
     *                  holds an open connection to the database.
     *           false  otherwise
     */ 
    public boolean connectToDatabase ()
    {
       try 
       {   
           /* connect to the database */
           conn = DriverManager.getConnection("jdbc:oracle:thin:@"+database,userid,passwd);
           return true;
       }
       catch (SQLException sql_ex) 
       {  
           /* error handling */

           System.out.println(sql_ex);
           return false;
       }
    }
    
    
    /**
     * open ONE single database connection
     */
    public boolean openConnection ()
    {
        boolean retrieve = true;
        
        if ( conn != null )
            System.err.println("You are already connected to Oracle; no second connection is needed!");
        else {
            if ( connectToDatabase() )
                System.out.println("You successfully connected to Oracle.");
            else {
                System.out.println("Oops - something went wrong.");
                retrieve = false;
            }
        }
        
        return retrieve;
    }

    /**
     * close the database connection again
     */
    public void closeConnection ()
    {
        if ( conn == null )
            System.err.println("You are not connected to Oracle!");
        else try
        {
             conn.close(); // close the connection again after usage! 
             conn = null;
        }
        catch (SQLException sql_ex) 
        {  /* error handling */
             System.out.println(sql_ex);
        }
    }
    
    
    /**
     * InsertAndUpdate extended function ((CU) of CRUD)
     * This function will include the insert or update statement
     */
    private void InsertAndUpdate(int type, String message, String statement, Issue issue){
    	//if type equals 0; then insert, else equals 1; then update
    	if(this.openConnection()){
			  try
		       {
		          /* prepare a dynamic query statement */
		          PreparedStatement stmt = conn.prepareStatement(statement);
		          stmt.setString(1, issue.getTitle());
		          stmt.setString(2, issue.getDescription());
		          stmt.setInt(3, issue.getCreator());
		          stmt.setInt(4, issue.getResolver());
		          stmt.setInt(5, issue.getVerifier());
		          //only do when type is equal 1
		          if(type==1){
		          stmt.setInt(6, issue.getId());
		          }
		          /* execute update or insert statement */
		          int effect = stmt.executeUpdate(); 
		                
		          if(effect==0){
		        	System.out.println(message+" failure!");  
		          }
		          else if (effect==1){
			        System.out.println(message+" success!");  

		          }
		          /* clean up! (NOTE this really belongs in a finally{} block) */
		          stmt.close();
		       }
			   catch(NullPointerException e){
		        	System.out.println("check your fields, some of them should not be null");  
		        	
			   }
			
		       catch (SQLException sqle) 
		       {  
		           /* error handling */
		        	System.out.println(message+" failure!");  

		           System.out.println("SQLException : " + sqle);
		       }
			   
			  this.closeConnection();
			}
    }
    
    /**
     * end
     */
    
    
    
    
	/**
	 * Update the details for a given issue
	 * @param issue : the issue for which to update details
	 */
	@Override
	public void updateIssue(Issue issue) {
		
		String updateStatement = "UPDATE A3_ISSUE SET TITLE=?, DESCRIPTION=?,CREATOR=?,RESOLVER=?,VERIFIER=? WHERE ID=?";
		this.InsertAndUpdate(1, "update", updateStatement, issue);
	
	}
	
	
	/**
	 * Query extended function ((R) of CRUD) - by store procedure
	 */
	private Vector<Issue> queryExtend(int userId, String storeProdureStatement,int condition){
		Vector<Issue> issueVec = new Vector<Issue>();

		CallableStatement callableStatement = null;
		ResultSet rs = null;

		if(openConnection()){
			  try
		       {
		          //prepare a dynamic query statement
				  callableStatement = conn.prepareCall(storeProdureStatement);
		                                   
				  callableStatement.setInt(1, userId);
				  callableStatement.registerOutParameter(2, OracleTypes.CURSOR);

				  //execute getAllUserIssues store procedure
				  callableStatement.executeUpdate();
				
				  //casting and get result set	
				  rs = (ResultSet) callableStatement.getObject(2);
						
		          int nr = 0;
		          while ( rs.next() )
		          {
		             nr++;
		             Issue tempIssue = new Issue();
		             tempIssue.setTitle(rs.getString("TITLE"));
		             tempIssue.setDescription(rs.getString("DESCRIPTION"));
		             tempIssue.setCreator(rs.getInt("CREATOR"));
		             if(!rs.wasNull()){
		            	 tempIssue.setId(rs.getInt("ID"));
			             tempIssue.setResolver(rs.getInt("RESOLVER"));
			             tempIssue.setVerifier(rs.getInt("VERIFIER"));
		             }
		          
		             issueVec.add(tempIssue);
		          }
		              
		          if ( nr == 0 )
		             System.out.println("No entries found.");
		                 
		          /* clean up! (NOTE this really belongs in a finally{} block) */
		          callableStatement.close();
		       }
		       catch (SQLException sqle) 
		       {  
		           /* error handling */
		           System.out.println("SQLException : " + sqle);
		       }
			  closeConnection();
			}
		
		return issueVec;
		
	}
	
	
	/**
	 * end
	 */
	
	

	/**
	 * Find the issues associated in some way with a user
	 * Issues which have the id parameter below in any one or more of the
	 * creator, resolver, or verifier fields should be included in the result
	 * @param id
	 * @return
	 */
	@Override
	public Vector<Issue> findUserIssues(int id) {
		//the 'id' from is display current login user's id
		return this.queryExtend(id, "{call getAllUserIssues(?,?)}", 0);
	}
	
	/**
	 * Add the details for a new issue to the database
	 * @param issue: the new issue to add
	 */
	@Override
	public void addIssue(Issue issue) {
		String insertStatement = "INSERT into A3_ISSUE(TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER)"+
	        		  "values (?,?,?,?,?)";
		this.InsertAndUpdate(0, "insert", insertStatement, issue);	
	}
	



	/**
	 * Given an expression searchString like myFirst words|my second words
	 * this method should return any issues associated with a user based on userId that either:
	 * contain 1 or more of the phrases separated by the '|' character in the issue title OR
	 * contain 1 or more of the phrases separated by the '|' character in the issue description OR
	 * @param searchString: the searchString to use for finding issues in the database based on the issue titles and
	 * descriptions. searchString may either be a single phrase, or a phrase separated by the '|' character. The searchString
	 * is used as described above to find matching issues in the database.
	 * @param userId: used to first find issues associated with userId on either one or more of the creator/resolver/verifier
	 * fields. Once a user's issues are identified, the search would then take place on the user's associated issues.
	 * @return
	 */
	@Override
	public Vector<Issue> findIssueBasedOnExpressionSearchedOnTitleAndDescription(
			String searchString, int userId) {
		Vector<Issue> newIssue = new Vector<Issue>();
		System.out.println(searchString);
		//A blank search 
		if(searchString.isEmpty()){
			newIssue = this.queryExtend(userId, "{call getAllUserIssues(?,?)}", 0);
		}
		return newIssue;
	}
	
	public Issue getDummyIssue()
	{
		Issue dummyIssue = new Issue();
		dummyIssue.setId(1);
		dummyIssue.setCreator(1);
		dummyIssue.setTitle("PlaceHolder issue");
		dummyIssue.setDescription("PlaceHolder Issue Description");
		return dummyIssue;
	}
	
	public Vector<Issue> getDummyIssues()
	{
		Vector<Issue> issues = new Vector<Issue>();
		issues.add(getDummyIssue());
		return issues;
	}
	
}
