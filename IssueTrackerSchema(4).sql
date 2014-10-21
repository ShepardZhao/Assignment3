CREATE TABLE A3_USER
(FIRSTNAME VARCHAR2(100) not null, 
LASTNAME VARCHAR2(100) not null, 
ID NUMBER GENERATED  AS IDENTITY primary key);
Insert into A3_USER (FIRSTNAME,LASTNAME) values ('Dean','Smith');
Insert into A3_USER (FIRSTNAME,LASTNAME) values ('Jess','Smith');
commit;

CREATE TABLE A3_ISSUE 
(ID NUMBER GENERATED ALWAYS AS IDENTITY primary key, 
DESCRIPTION VARCHAR2(1000), 
PROJECTID NUMBER, 
TITLE VARCHAR2(100),  
CREATOR NUMBER not null REFERENCES A3_USER, 
RESOLVER NUMBER REFERENCES A3_USER, 
VERIFIER NUMBER REFERENCES A3_USER);

Insert into A3_ISSUE (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER) values ('Division by zero','Division by 0 doesn''t yield error or infinity as would be expected. Instead it results in -1.',1,1,1);

Insert into A3_ISSUE (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER) values ('Factorial with addition anomaly','Performing a factorial and then addition produces an off by 1 error',1,1,1);

Insert into A3_ISSUE (TITLE,DESCRIPTION,CREATOR,RESOLVER,VERIFIER) values ('Incorrect’ BODMAS order','Addition occurring before multiplication',1,1,1);

commit;



