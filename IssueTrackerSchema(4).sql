DROP TABLE A3_ISSUE;
DROP TABLE A3_USER;


CREATE TABLE A3_USER
(FIRSTNAME VARCHAR2(100) not null, 
LASTNAME VARCHAR2(100) not null, 
ID NUMBER GENERATED  AS IDENTITY primary key);


CREATE TABLE A3_ISSUE 
(ID NUMBER GENERATED ALWAYS AS IDENTITY primary key, 
DESCRIPTION VARCHAR2(1000), 
PROJECTID NUMBER, 
TITLE VARCHAR2(100),  
UserVersionID int, -- this column for optimistic offline lock --
CREATOR NUMBER not null REFERENCES A3_USER, 
RESOLVER NUMBER REFERENCES A3_USER, 
VERIFIER NUMBER REFERENCES A3_USER);




-- create inital trigger to generate the version id --
create or replace trigger A3_ISSUE_Trigger_initial before insert on A3_ISSUE for each row 
begin
 --set the initial transaction control number --
	:new.UserVersionID := dbms_utility.get_time;
end;
-- end --



-- create the upadte version id in the trigger --
create or replace trigger A3_ISSUE_Trigger_update before update on A3_ISSUE for each row
begin
 -- update the transaction control number --	
	if( :new.UserVersionID != :old.UserVersionID+1 )  
	then
	raise_application_error( -20000, ‘Update Failure, another user is in progress’ ); 
	end if;
	
 -- update the transcation controller number --
 	:new.UserVersionID := dbms_utility.get_time;
end; 	
	

-- end --





Insert into A3_USER (FIRSTNAME,LASTNAME) values ('Dean','Smith');
Insert into A3_USER (FIRSTNAME,LASTNAME) values ('Jess','Smith');
commit;


Insert into A3_ISSUE (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER) values ('Division by zero','Division by 0 doesn''t yield error or infinity as would be expected. Instead it results in -1.',1,1,1);

Insert into A3_ISSUE (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER) values ('Factorial with addition anomaly','Performing a factorial and then addition produces an off by 1 error',1,1,1);

Insert into A3_ISSUE (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER) values ('Incorrect’ BODMAS order','Addition occurring before multiplication',1,1,1);

Insert into A3_ISSUE (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER) values ('asdasdasd’ BODMAS order','Addition occurring before multiplication',1,1,1);

Insert into A3_ISSUE (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER) values ('Incorrect’ BODMAS order','Addition occurring before multiplication',2,2,2);

commit;






-- Grant privilege -- 

GRANT ALL ON A3_ISSUE TO xzha4611;
GRANT ALL ON A3_USER TO xzha4611;
GRANT EXECUTE ON getAllUserIssues TO xzha4611;
GRANT EXECUTE ON SEARCHTYPE1 TO xzha4611;
-- end --


--  query store procedure - getAllUserIssues --

create or replace PROCEDURE getAllUserIssues(
	   p_userID IN A3_ISSUE.CREATOR %TYPE,
	   c_cursorIssues OUT SYS_REFCURSOR)
IS
BEGIN
 
  OPEN c_cursorIssues FOR
  SELECT * FROM A3_ISSUE WHERE CREATOR = p_userID OR "RESOLVER"=p_userID OR VERIFIER=p_userID;
 
END;

-- end --


-- search store procedure --
create or replace PROCEDURE SEARCHTYPE1(
  searchType IN INTEGER,
  searchName IN VARCHAR2,
  searchTitle1 IN VARCHAR2,
  searchTitle2 IN VARCHAR2,
  userID IN INTEGER,
  cusorIssue OUT SYS_REFCURSOR)
IS 
BEGIN
  IF searchType=1 THEN
    OPEN cusorIssue FOR
      SELECT DISTINCT TITLE,i.CREATOR,i.DESCRIPTION,i.ID,i.RESOLVER,i.VERIFIER FROM A3_USER u JOIN A3_ISSUE i ON
                    ((u.ID=i.CREATOR OR u.ID=i.RESOLVER OR u.ID=i.VERIFIER)
                    AND (i.VERIFIER=userID OR i.CREATOR=userID OR i.RESOLVER=userID))
        					  WHERE FIRSTNAME=searchName OR LASTNAME=searchName;
  ELSIF searchType=2 THEN
    OPEN cusorIssue FOR
        SELECT DISTINCT TITLE,i.CREATOR,i.DESCRIPTION,i.ID,i.RESOLVER,i.VERIFIER  FROM A3_USER u JOIN A3_ISSUE i ON
                    ((u.ID=i.CREATOR OR u.ID=i.RESOLVER OR u.ID=i.VERIFIER)
                    AND (i.VERIFIER=userID OR i.CREATOR=userID OR i.RESOLVER=userID))
                    WHERE (FIRSTNAME=searchName OR LASTNAME=searchName)
                    AND (TITLE like searchTitle1 OR DESCRIPTION like searchTitle1);
  ELSIF searchType=3 THEN
    OPEN cusorIssue FOR
        SELECT DISTINCT TITLE,i.CREATOR,i.DESCRIPTION,i.ID,i.RESOLVER,i.VERIFIER  FROM A3_USER u JOIN A3_ISSUE i ON
                    ((u.ID=i.CREATOR OR u.ID=i.RESOLVER OR u.ID=i.VERIFIER)
                    AND (i.VERIFIER=userID OR i.CREATOR=userID OR i.RESOLVER=userID))
                    WHERE (FIRSTNAME=searchName OR LASTNAME=searchName) 
                    AND ((TITLE like searchTitle1 OR DESCRIPTION like searchTitle1)
                          OR (TITLE like searchTitle2 OR DESCRIPTION like searchTitle2));
  ELSIF searchType=4 THEN
    OPEN cusorIssue FOR
        SELECT DISTINCT TITLE,i.CREATOR,i.DESCRIPTION,i.ID,i.RESOLVER,i.VERIFIER  FROM A3_USER u JOIN A3_ISSUE i ON
                    ((u.ID=i.CREATOR OR u.ID=i.RESOLVER OR u.ID=i.VERIFIER)
                    AND (i.VERIFIER=userID OR i.CREATOR=userID OR i.RESOLVER=userID))
                    WHERE (TITLE like searchTitle1) OR (DESCRIPTION like searchTitle1) ;
  ELSIF searchType=5 THEN
    OPEN cusorIssue FOR
        SELECT DISTINCT TITLE,i.CREATOR,i.DESCRIPTION,i.ID,i.RESOLVER,i.VERIFIER  FROM A3_USER u JOIN A3_ISSUE i ON
                    ((u.ID=i.CREATOR OR u.ID=i.RESOLVER OR u.ID=i.VERIFIER)
                    AND (i.VERIFIER=userID OR i.CREATOR=userID OR i.RESOLVER=userID))
                    WHERE (TITLE like searchTitle1 OR DESCRIPTION like searchTitle1)
                    OR (TITLE like searchTitle2 OR DESCRIPTION like searchTitle2);
  ELSE
      DBMS_OUTPUT.PUT_LINE('NORTHING HAPPEN');
  END IF;      
END SEARCHTYPE1;

-- end --













