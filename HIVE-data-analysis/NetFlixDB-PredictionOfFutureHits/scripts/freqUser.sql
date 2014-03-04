CREATE TABLE IF NOT EXISTS NF_Rate (MID int, CID int, Rating int, Date string) COMMENT 'Netflix Ratings Table' ROW FORMAT Delimited fields terminated by ',' stored as textfile location 's3n://spring-2014-ds/movie_dataset/movie_ratings/';
CREATE TABLE IF NOT EXISTS NF_Titles (MID int, YOR int, Title STRING) COMMENT 'Netflix movie Table' ROW FORMAT Delimited fields terminated by ',' stored as textfile location 's3n://spring-2014-ds/movie_dataset/movie_titles/';
create table if not exists NF_movie as select * from NF_Titles where YOR between 2001 and 2005;
create table if not exists NF_Ratings as select * from NF_Rate where year(Date) between 2001 and 2005;
create table perUserRate as select mov.mid, mov.title, rate.cid, rate.rating from NF_movie mov join NF_Ratings rate on mov.mid = rate.mid where rate.rating is not null;
create table if not exists avg_rate as select mr.mid, mr.title, avg(mr.rating) as avgR from perUserRate mr group by mr.mid, mr.title order by avgR;
create table freqRaters as select * from (select sr.cid, count(*) as mov_count, rank() over (order by count(*) desc) as rank from perUserRate sr group by sr.cid) x where x.rank between 1 and 500;
create table if not exists potentialUserRating as select us.mid, us.title, fu.cid, (case when ar.avgR is null or us.rating is null then 0.0 else abs(ar.avgR - us.rating) end) as deviation from perUserRate us join avg_rate ar on us.mid = ar.mid join freqRaters fu on us.cid = fu.cid;
create table if not exists avgDev as select cid, avg(deviation) as avgDev from potentialUserRating group by cid order by avgDev;
create external table potentialUser (cid int, potfactor double) row format delimited fields terminated by ',' lines terminated by '\n' stored as textfile location 's3n://hive1sanchita/freqUser/user/';
insert overwrite table potentialUser select fu.cid, (fu.mov_count/ad.avgDev) as potfactor from freqRaters fu join avgDev ad on ad.cid = fu.cid order by potfactor desc;
create table potentialMovieGenre as select mov.title movie, r.rating, pu.cid, pu.potfactor, (pu.potfactor * r.rating) as moviepot from NF_Ratings r join NF_movie mov on (mov.mid = r.mid) join potentialUser pu on (pu.cid = r.cid) where r.rating > 3 and pu.potfactor > 2500 order by moviepot desc;
create external table moviepotential (title string, potfactor double) row format delimited fields terminated by ',' lines terminated by '\n' stored as textfile location 's3n://hive1sanchita/freqUser/moviepot/';
insert overwrite table moviepotential select movie, sum(moviepot)+count(movie) as potfactor from potentialMovieGenre group by movie order by potfactor desc;

