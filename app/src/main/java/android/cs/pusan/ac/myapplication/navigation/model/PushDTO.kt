package android.cs.pusan.ac.myapplication.navigation.model

data class PushDTO(
        var to : String? = null,    //push 받는 사람의 토큰 아이디
        var notification : Notification = Notification()
){
    data class Notification(
            var body : String? = null,      //푸시메세지의 내용
            var title : String? = null      //푸시메세지의 제목
    )
}