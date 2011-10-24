mvn test -Djub.customkey=$1 -Djub.consumers=CONSOLE,XML,H2 -Djub.db.file=target/benchmarks/database -Djub.xml.file=target/logs/benchmarks.xml -Djub.charts.dir=target/data/benchmarks/graphs
