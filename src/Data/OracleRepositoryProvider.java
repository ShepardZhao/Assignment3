package Data;


import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import Business.Issue;
import oracle.jdbc.OracleTypes;
import Presentation.IRepositoryProvider;

/**
 * Encapsulates create/read/update/delete operations to Oracle database
 * @author matthewsladescu
 */
public class OracleRepositoryProvider implements IRepositoryProvider {
	   // connection parameters - ENTER YOUR LOGIN AND PASSWORD HERE
    private final String userid   = "xzha4611";
    private final String passwd   = "xzha4611";
    private final String database = "oracle12.it.usyd.edu.au:1521:COMP5138";
    // instance variable for the database connection   
    private Connection conn = null; 
	private ArrayList<String> names = new ArrayList<String>();
	private ArrayList<String> titleOrDescription = new ArrayList<String>();
	private String searchName="";
	private String searchTitle1="";
	private String searchTitle2="";
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
				  conn.setAutoCommit(false);
		          /* prepare a dynamic query statement */
		          PreparedStatement stmt = conn.prepareStatement(statement);
		          stmt.setString(1, issue.getTitle());
		          stmt.setString(2, issue.getDescription());
		          stmt.setInt(3, issue.getCreator());
		          stmt.setInt(4, issue.getResolver());
		          stmt.setInt(5, issue.getVerifier());          
		          //only do when type is equal 1
		          if(type==1){
		        	//  stmt.setInt(6, issue.getId());
		        	  stmt.setInt(6, issue.getId());
//		        	  stmt.setInt(7, issue.getResolver());
//		        	  stmt.setInt(8, issue.getVerifier());	
//		        	  stmt.setInt(9, issue.getId());
		          }
		          /* execute update or insert statement */
		          stmt.executeUpdate(); 
		          
		          System.out.println("Insert/Update success!");
		          
		          /* clean up! (NOTE this really belongs in a finally{} block) */
		          stmt.close();
		       }
			   catch(NullPointerException e){
		        	System.out.println("check your fields, some of them should not be null");  
		        	
			   }
			
		       catch (SQLException sqle) 
		       {  
		           /* error handling */
		    	   try{
		    		   System.out.println("SQLException:"+sqle);
			    	   System.out.println(message+" failure! ROLLBACK!!!!");  
			           System.out.println("Transaction is being rolled back");
			           conn.rollback();
		    	   }catch(SQLException excep){
		    		   System.out.println("SQLException:"+excep);
		    	   }
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
		
//		String updateStatement = "UPDATE A3_ISSUE i"+
//				" SET i.TITLE=?, i.DESCRIPTION=?,i.CREATOR=?,i.RESOLVER=?,i.VERIFIER=?"+
//				" WHERE EXISTS ("+
//				" SELECT *"+
//				" FROM A3_USER u"+
//				" WHERE (u.ID=i.CREATOR OR u.ID=i.RESOLVER OR u.ID=i.VERIFIER) AND (u.ID=? OR u.ID=? OR u.ID=?)"+
//				" AND (i.ID=?))";
		
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
				  if(condition == 0){
			          //prepare a dynamic query statement
					  callableStatement = conn.prepareCall(storeProdureStatement);
			                                   
					  callableStatement.setInt(1, userId);
					  callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
					  //execute getAllUserIssues store procedure
					  callableStatement.executeUpdate();
					
					  //casting and get result set	
					  rs = (ResultSet) callableStatement.getObject(2);
				  }
				  else{
					  callableStatement = conn.prepareCall(storeProdureStatement);
					  callableStatement.setInt(1, condition);
					  callableStatement.setString(2, searchName);
					  callableStatement.setString(3, "%"+searchTitle1+"%");
					  callableStatement.setString(4, "%"+searchTitle2+"%");
					  callableStatement.setInt(5, userId);
					  callableStatement.registerOutParameter(6, OracleTypes.CURSOR);
					  //execute search store procedure
					  callableStatement.executeUpdate();
					
					  //casting and get result set	
					  rs = (ResultSet) callableStatement.getObject(6);
				  }

						
		          int nr = 0;
		          while ( rs.next() )
		          {
		             nr++;
		             Issue tempIssue = new Issue();
		             tempIssue.setTitle(rs.getString("TITLE"));
		             tempIssue.setDescription(rs.getString("DESCRIPTION"));
		             tempIssue.setCreator(rs.getInt("CREATOR"));
		             tempIssue.setId(rs.getInt("ID"));
			         tempIssue.setResolver(rs.getInt("RESOLVER"));
			         tempIssue.setVerifier(rs.getInt("VERIFIER"));		             		          
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
		String insertStatement = "INSERT into A3_ISSUE i (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER)"+
	        		" values (?,?,?,?,?)";
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
		//A blank search 
		if(searchString.isEmpty()){
			newIssue = this.queryExtend(userId, "{call getAllUserIssues(?,?)}", 0);
		}else{	
			identify(searchString);
			if(titleOrDescription.size()>0)
				searchTitle1=titleOrDescription.get(0);
			if(titleOrDescription.size()>1)
				searchTitle2=titleOrDescription.get(1);
			          /* prepare a dynamic query statement */
			if(names.size() > 0){
				searchName=names.get(0);
				if(titleOrDescription.size()==0){
					newIssue = this.queryExtend(userId, "{call SEARCHTYPE1(?,?,?,?,?,?)}", 1);
				}
				else if(titleOrDescription.size()==1){
					newIssue = this.queryExtend(userId, "{call SEARCHTYPE1(?,?,?,?,?,?)}", 2);
				}else{
					newIssue = this.queryExtend(userId, "{call SEARCHTYPE1(?,?,?,?,?,?)}", 3);
				}			  
			}else{
				if(titleOrDescription.size()==1){
					newIssue = this.queryExtend(userId, "{call SEARCHTYPE1(?,?,?,?,?,?)}", 4);
					
					
				}else if(titleOrDescription.size()>1){
					newIssue = this.queryExtend(userId, "{call SEARCHTYPE1(?,?,?,?,?,?)}", 5);
				}
			}		  
			
		}			
		
		return newIssue;
	}
	


	public void identify(String searchStr){
		ArrayList<String> list = new ArrayList<String>();

		boolean hasAt = false;
		if(searchStr.contains("@")) hasAt = true;
		
		String[] str = searchStr.split("@");
		for(String s : str){
			if(!s.isEmpty()){
				list.add(s);
			}
		}
		int index = 0;
		if(hasAt){
			for(String s:list){
				String[] temp = s.split("\\|");
				if(index == 0){				
					for(String s1:temp){
						names.add(s1);
					}
				}else if(index == 1){
					for(String s1:temp){
						titleOrDescription.add(s1);
					}
				}
				index++;
			}
		}else{
			for(String s:list){
				String[] temp = s.split("\\|");
				for(String s1:temp){
					titleOrDescription.add(s1);
				}
			}
		}
		for(String s:names){
			System.out.print("Name: ");
			System.out.println(s);
		}
		for(String s:titleOrDescription){
			System.out.print("TitleOrDescription: ");
			System.out.println(s);
		}
	}
}
