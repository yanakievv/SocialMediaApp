package com.example.socialmediaappv2.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.data.App
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import kotlinx.coroutines.runBlocking

private lateinit var presenter: com.example.socialmediaappv2.contract.Contract.ProfileInfoPresenter
private const val ARG_PARAM1 = "uId"

class EditProfileFragment : Fragment(), com.example.socialmediaappv2.contract.Contract.ProfileView {
    private var uId: String? = null
    private var regexDate: Regex = "^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]|(?:Jan|Mar|May|Jul|Aug|Oct|Dec)))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2]|(?:Jan|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})\$|^(?:29(\\/|-|\\.)(?:0?2|(?:Feb))\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))\$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9]|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep))|(?:1[0-2]|(?:Oct|Nov|Dec)))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})\$".toRegex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uId = it.getString(ARG_PARAM1)
        }
        setPresenter(ProfileInfoPresenter(this))
        runBlocking { context?.let { presenter.init(uId!!, it) } }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        view.cancelChanges.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
        if (uId != App.currentUser.publisherId) { // check to confirm that we are working with current users's data(shouldn't be used, but to be sure)
            view.cancelChanges.performClick()
        }
        view.editDisplayName.setText(App.currentUser.displayName, TextView.BufferType.EDITABLE)
        if (App.currentUser.birthDate == "Private") {
            view.privateBirthDate.isChecked = true
        }
        else view.editBirthDate.setText(App.currentUser.birthDate, TextView.BufferType.EDITABLE)
        view.editBio.setText(App.currentUser.bio, TextView.BufferType.EDITABLE)
        view.saveChanges.setOnClickListener {
            if (!view.privateBirthDate.isChecked && !view.editBirthDate.text.toString().matches(regexDate))
            {
                Toast.makeText(context, "Birth date must match DD/MM/YYYY.", Toast.LENGTH_SHORT).show()
            }
            else {
                presenter.setDisplayName(view.editDisplayName.text.toString())
                if (view.privateBirthDate.isChecked) {
                    presenter.setBirthDate("Private")
                }
                else presenter.setBirthDate(view.editBirthDate.text.toString())
                presenter.setBio(view.editBio.text.toString())
                update()
                view.cancelChanges.performClick()
            }
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(uId: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, uId)
                }
            }
    }

    override fun update() {
        presenter.refreshDb()
        (activity as ProfileActivity).update()
    }

    override fun setPresenter(_presenter: com.example.socialmediaappv2.contract.Contract.ProfileInfoPresenter) {
        presenter = _presenter
    }
}