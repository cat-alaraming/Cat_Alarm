package android.cs.pusan.ac.myapplication.model

data class PushDTO(
        var to : String? = null,    //push 받는 사람의 토큰 아이디
        var notification : Notification = Notification()
){
    data class Notification(
            var body : String? = null,
            var title : String? = null
    )
}