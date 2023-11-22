package id.co.psplauncher.data.network.response

data class User(
    val address: String,
    val dateOfBirth: String,
    val email: String,
    val gender: String,
    val id: String,
    val lastCompanyId: String,
    val lastService: String,
    val maritalStatus: String,
    val name: String,
    val nik: String,
    val phone: Any,
    val photoUrl: String,
    val placeOfBirth: String,
    val regDate: String,
    val religion: String,
    val username: String,
    val validationStatus: Boolean
)
