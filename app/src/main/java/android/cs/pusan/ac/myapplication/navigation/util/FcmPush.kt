package android.cs.pusan.ac.myapplication.navigation.util

import android.cs.pusan.ac.myapplication.BuildConfig
import android.cs.pusan.ac.myapplication.navigation.model.PushDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.sql.DriverManager.println


class FcmPush {

    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = BuildConfig.SERVER_KEY
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null

    //싱글톤 패턴?으로 어디서든지 사용하기 쉽게
    companion object{
        @JvmField var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    //push 메세지 전송해주는 함수
    fun sendMessage(destinationUid : String, title : String, message : String){
        //상대방의 uid를 이용해서 pushtoken을 받아옴
        //firestore에 있는 pushtoken 컬랙션에 접근해서 얻어옴
        FirebaseFirestore.getInstance().collection("pushTokens").document("userTocken").get().addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                var token = task?.result?.get("pushToken").toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                var body = RequestBody.create(JSON,gson?.toJson(pushDTO))
                var request = Request.Builder()
                        .addHeader("Content-Type","application/json")
                        .addHeader("Authorization","key="+serverKey)
                        .url(url)
                        .post(body)
                        .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {

                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        println(response?.body()?.string())
                    }

                })
            }
        }
    }

}