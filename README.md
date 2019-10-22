# reactive-rabbitmq-redis-demo

rabbitmq와 redis의 reactive 버전을 활용하여 간단한 애플리케이션 데모 구현. 애플리케이션이 하는 일을 API로 소개하면 아래와 같음.

*POST /account/{accountNumber}/transfer*

- 계좌의 거래 이벤트인 `MoneyTransferred`를 받아,
- 잔고를 갱신하고,
- 다시 rabbitmq로 거래 이벤트를 전달함. (스트리밍을 위함)

*GET /account/{accountNumber}/transfer*

- 계좌의 잔고를 보여줌.

*GET /account/{accountNumber}/transfer/stream*

- rabbitmq로 들어오는 계좌의 거래 이벤트를 실시간으로 스트리밍
