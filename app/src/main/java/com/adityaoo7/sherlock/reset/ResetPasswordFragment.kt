package com.adityaoo7.sherlock.reset

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.databinding.FragmentResetPasswordBinding
import com.google.android.material.snackbar.Snackbar

class ResetPasswordFragment : Fragment() {

    private lateinit var binding: FragmentResetPasswordBinding

    private val resetPasswordViewModel by viewModels<ResetPasswordViewModel> {
        ResetPasswordViewModelFactory(
            (requireContext().applicationContext as SherlockApplication).accountsRepository,
            (requireContext().applicationContext as SherlockApplication).sharedPreferencesManager,
            (requireContext().applicationContext as SherlockApplication).encryptionService

        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentResetPasswordBinding.inflate(inflater, container, false).apply {
            viewModel = resetPasswordViewModel
        }

        resetPasswordViewModel.snackbarText.observe(viewLifecycleOwner, { string ->
            if (string != null) {
                Snackbar.make(requireView(), getString(string), Snackbar.LENGTH_SHORT).show()
                resetPasswordViewModel.doneShowingSnackbar()
            }
        })

        resetPasswordViewModel.navigateToHomeScreen.observe(viewLifecycleOwner, { navigate ->
            if (navigate) {
                findNavController().navigate(
                    ResetPasswordFragmentDirections.actionResetPasswordFragmentToHomeFragment()
                )
                findNavController().popBackStack(R.id.resetPasswordFragment, true)
                resetPasswordViewModel.doneNavigating()
            }
        })

        resetPasswordViewModel.dataLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                binding.resetPasswordLayout.visibility = View.GONE
                binding.processingResetPasswordLayout.visibility = View.VISIBLE
            } else {
                binding.resetPasswordLayout.visibility = View.VISIBLE
                binding.processingResetPasswordLayout.visibility = View.GONE
            }
        })

        return binding.root
    }
}