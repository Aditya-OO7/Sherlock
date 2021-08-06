package com.adityaoo7.sherlock.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar


class RegisterFragment : Fragment() {

    private val TAG = RegisterFragment::class.java.simpleName

    private lateinit var binding: FragmentRegisterBinding

    private val authViewModel by activityViewModels<AuthenticationViewModel> {
        AuthenticationViewModelFactory(
            (requireContext().applicationContext as SherlockApplication).sharedPreferencesManager,
            (requireContext().applicationContext as SherlockApplication).encryptionService
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
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

        authViewModel.dataLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                binding.loadingRegisterLayout.visibility = View.VISIBLE
                binding.enterPasswordEditText.visibility = View.INVISIBLE
                binding.reEnterPasswordEditText.visibility = View.INVISIBLE
            } else {
                binding.enterPasswordEditText.visibility = View.VISIBLE
                binding.reEnterPasswordEditText.visibility = View.VISIBLE
                binding.loadingRegisterLayout.visibility = View.GONE
            }
        })

        authViewModel.navigateToLoginScreen.observe(viewLifecycleOwner, { navigate ->
            if (navigate) {
                Toast.makeText(requireContext(), R.string.register_success, Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
                findNavController().popBackStack(R.id.registerFragment, true)
                authViewModel.doneNavigatingToLoginScreen()
            }
        })

        return binding.root
    }
}