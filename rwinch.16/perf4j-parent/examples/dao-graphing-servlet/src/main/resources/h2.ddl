create table perf4j_stopwatches (id varchar(64) primary key, message varchar(32), elapsed_time BIGINT, start_time BIGINT, tag LONGVARCHAR);
create table perf4j_groupedtimingstatistics (id varchar(64) primary key, start_time BIGINT, stop_time BIGINT);
create table perf4j_timingstatistics (id varchar(64) primary key, perf4j_gts_id varchar(64), count_stat BIGINT, max_stat BIGINT, mean_stat BIGINT, min_stat BIGINT, std_deviation_stat BIGINT, tag LONGVARCHAR);
alter table perf4j_timingstatistics add constraint fkgroupedtimingstatistics foreign key (perf4j_gts_id) references perf4j_groupedtimingstatistics (id);
create sequence perf4j_sequence start with 1 increment by 1;