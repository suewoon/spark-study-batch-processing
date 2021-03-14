# spark-study-batch-processing
배치처리 스터디 
## ecomm
* elastic search 에 저장된 인덱스를 가져와서 간단한 처리 후 다시 elatic search에 `ecomm_data.order.1d.${날찌}` 인덱스에 저장 
* kibana에서 제공하는 sample data `kibana_sample_data_ecommerce` 사용
* argument로 `es.nodes` 주소를 받음
* 로컬에서 테스트 목적을 위해 작성. YARN 등 스파크 클러스터 매니저를 이용할 경우, `master("local[*]")` 코드 삭제 필요 


## 빌드 방법
project 아래 `assembly.sbt` 추가 
```
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")
```
sbt shell에서 아래 명령어 실행 
```
assembly
```
`/tmp/sbt/ecomm-analyzer/scala-2.11` 아래 `ecomm-analyzer-assembly-0.1.jar`를 찾을 수 있음

## spark-submit
```
${SPARK_HOME}/bin/spark-submit \
--name ecomm.BatchProducerRunnerSpark \
--class ecomm.BatchProducerRunnerSpark \
ecomm-analyzer-assembly-0.1.jar "${ES_NODES}"
```