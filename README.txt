If you have Eclipse, download the STS plug-in from https://marketplace.eclipse.org/content/spring-tools-aka-spring-ide-and-spring-tool-suite
Now to test our app, we will use SOAPUI. You can download SOAPUI from https://www.soapui.org/downloads/soapui.html

recompile using cmd : mvnw clean package
target jar will be in /target
run jar using cmd   : java -jar target\spring-boot-rest-0.0.1-SNAPSHOT.jar 

after server is running.....




TESTING server:-
simple test: http://localhost:8083/welcome/user?name=rahul
reply: welcome message 

TESTING Dineout Recommender Assignment:-

request by GET: use this to encode ur matrix:-https://www.url-encode-decode.com/
http://localhost:8083/dineoutRecommender/assignment?matrix=%5B%5B0.1%2C0.5%2C0.8%5D%2C%5B0.5%2Cx%2C0.3%5D%5D

request by POST:
URL:-     http://localhost:8083/dineoutRecommender/findX
media type:application/json DATA:-
{
            "input": "[[5,x,3,x,x,5],[x,3,x,0,4.5,2],[x,x,2,x,5,x],[4,x,x,x,x,x],[3,x,3,x,x,x]]"
}


