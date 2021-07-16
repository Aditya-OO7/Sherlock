package com.adityaoo7.sherlock.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.databinding.FragmentAuthenticationBinding
import com.google.android.material.snackbar.Snackbar


class AuthenticationFragment : Fragment() {

    private lateinit var binding: FragmentAuthenticationBinding
    private val authViewModel by viewModels<AuthenticationViewModel> {
        AuthenticationViewModelFactory(
            (requireContext().applicationContext as SherlockApplication).sharedPreferencesManager,
            (requireContext().applicationContext as SherlockApplication).encryptionService
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthenticationBinding.inflate(inflater, container, false)
            .apply {
                this.viewModel = authViewModel
            }

        binding.lifecycleOwner = this.viewLifecycleOwner

        authViewModel.snackbarText.observe(viewLifecycleOwner, { string ->
            if (string != null) {
                Snackbar.make(requireView(), getString(string), Snackbar.LENGTH_SHORT).show()
                authViewModel.doneShowingSnackbar()
            }
        })

        authViewModel.isRegistered.observe(viewLifecycleOwner, { isRegistered ->
            if (isRegistered != null) {
                if (isRegistered) {
                    binding.headingTextView.text = getString(R.string.login_heading_text_view)
                    binding.submitButton.text = getString(R.string.login_button_text)
                    binding.reEnterPasswordEditText.visibility = View.GONE
                } else {
                    binding.headingTextView.text = getString(R.string.register_heading_text_view)
                    binding.submitButton.text = getString(R.string.register_button_text)
                    binding.reEnterPasswordEditText.visibility = View.VISIBLE
                }
            }
        })

        authViewModel.dataLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                binding.authProgressBar.visibility = View.VISIBLE
            } else {
                binding.authProgressBar.visibility = View.GONE
            }
        })

        authViewModel.navigateToHomeScreen.observe(viewLifecycleOwner, { navigate ->
            if (navigate) {
                findNavController().navigate(
                    AuthenticationFragmentDirections.actionAuthenticationFragmentToHomeFragment()
                )
                authViewModel.doneNavigating()
            }
        })

        return binding.root
    }
}