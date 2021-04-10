
@echo off

echo Indexing on: %date% %time%

java -Xmx256M -Dopt=index -Denv=http://localhost:8080 -Durl=/solr-example/collection1/update/csv -Dsrc=D:/solr/example/solr/collection1/ -cp loganalyzer.jar com.amerigroup.loganalyzer.SolrMain "2013-08-06"

