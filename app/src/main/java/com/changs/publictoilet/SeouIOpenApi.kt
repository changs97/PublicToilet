package com.changs.publictoilet

import com.changs.publictoilet.data.Toilet
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

//Open API를 사용하기 위한 기본 정보를 담아두는 클래스
class SeoulOpenApi {
    companion object {
        val DOMAIN = "http://openapi.seoul.go.kr:8088/" //도메인 주소
        val API_KEY = "4c53416d4c6b696e3130315051504156" // API 키

    }
}
//레트로핏에서 사용할 인터페이스
interface SeoulOpenService {
    @GET("{api_key}/json/SearchPublicToiletPOIService/1/1000") //@GET 애노테이션을 사용해서 호출할 주소를 지정
    fun getToilet(@Path("api_key") key:String): Call<Toilet> //화장실 데이터를 가져오는 메서드
}//@Path 애노테이션을 사용하면 메서드의 파라미터로 넘어온 값을 @GET에 정의된 주소에 동적으로 삽입 가능