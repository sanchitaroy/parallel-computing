CREATE TABLE IF NOT EXISTS EnronEmail (EID STRING, timestamp STRING, frm STRING, to STRING, cc STRING, subject STRING, context STRING) COMMENT 'Enron Email Table' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' STORED AS TEXTFILE LOCATION 's3n://spring-2014-ds/enron_dataset/';
create external table emailRelations (company string, year int, tocount int, fromcount int, quotient float) row format delimited fields terminated by ',' lines terminated by '\n' stored as textfile location 's3n://hive1sanchita/companyrelations/results/';
insert overwrite table emailRelations select (case when A.Rto is null then B.Rfrom else A.Rto end) as company, (case when A.Rto is null then B.fromyear else A.toyear end) as year, A.tocount, B.fromcount, (case when A.tocount is null or B.fromcount is null then 0.0 else ((1.5*B.fromcount)+A.tocount)/2 end) as quotient from (select X.Rto as Rto, count(X.Rto) as tocount, X.year as toyear from (select regexp_extract(rec, '@(.*)$', 1) as Rto, regexp_extract(timestamp, '[0-9][0-9][0-9][0-9]', 0) as year FROM enronemail LATERAL VIEW explode(split(substr(to, 3, length(to) - 2), ',')) t1 AS rec WHERE frm like '%enron%') X group by X.Rto, X.year order by toyear) A full outer join (select X.Rfrom as Rfrom, count(X.Rfrom) as fromcount, X.year as fromyear from (select regexp_extract(frm, '@(.*)$', 1) as Rfrom, regexp_extract(timestamp, '[0-9][0-9][0-9][0-9]', 0) as year FROM enronemail WHERE frm not like '%enron%') X group by X.Rfrom, X.year order by fromyear) B on A.Rto = B.Rfrom and A.toyear = B.fromyear order by quotient desc;
create external table successemail (company string, quotient float) row format delimited fields terminated by ',' lines terminated by '\n' stored as textfile location 's3n://hive1sanchita/companyrelations/results/aggregaterelationship/';
insert overwrite table successemail select company, sum(quotient)*count(quotient) as successquot from emailRelations group by company order by successquot desc;