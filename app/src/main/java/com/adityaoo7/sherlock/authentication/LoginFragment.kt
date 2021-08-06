package com.adityaoo7.sherlock.authentication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.adityaoo7.sherlock.MainActivity
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.data.Result
import com.adityaoo7.sherlock.data.succeeded
import com.adityaoo7.sherlock.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private val TAG = LoginFragment::class.java.simpleName

    private lateinit var binding: FragmentLoginBinding

    private val authViewModel by activityViewModels<AuthenticationViewModel> {
        AuthenticationViewModelFactory(
            (requireContext().applicationContext as SherlockApplication).sharedPreferencesManager,
            (requireContext().applicationContext as SherlockApplication).encryptionService
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref =
            (requireContext().applicationContext as SherlockApplication).sharedPreferencesManager
        val result = pref.getIsRegistered()
        if (result.succeeded) {
            val isRegistered = (result as Result.Success).data
            if (!isRegistered) {
                findNavController()
                    .navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            viewModel = authViewModel
        }
        binding.lifecycleOwner = this.viewLifecycleOwner

        authViewModel.snackbarText.observe(viewLifecycleOwner, { string ->
            if (string != null) {
                Snackbar.make(requireView(), getString(string), Snackbar.LENGTH_SHORT).show()
                authViewModel.doneShowingSnackbar()
            }
        })

        authViewModel.dataLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                binding.loadingLoginLayout.visibility = View.VISIBLE
                binding.loginPasswordEditText.visibility = View.INVISIBLE
            } else {
                binding.loginPasswordEditText.visibility = View.VISIBLE
                binding.loadingLoginLayout.visibility = View.GONE
            }
        })

        authViewModel.navigateToHomeScreen.observe(viewLifecycleOwner, { navigate ->
            if (navigate) {
                Toast.makeText(requireContext(), R.string.auth_success, Toast.LENGTH_LONG).show()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finishAffinity()
                authViewModel.doneNavigatingToHomeScreen()
            }
        })

        return binding.root
    }
}