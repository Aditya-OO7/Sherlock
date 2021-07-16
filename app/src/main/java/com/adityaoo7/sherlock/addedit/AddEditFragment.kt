package com.adityaoo7.sherlock.addedit

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.databinding.FragmentAddEditBinding
import com.google.android.material.snackbar.Snackbar

class AddEditFragment : Fragment() {

    private lateinit var binding: FragmentAddEditBinding

    private val args: AddEditFragmentArgs by navArgs()

    private var toggle = false

    private val addEditViewModel by viewModels<AddEditViewModel> {
        AddEditViewModelFactory(
            (requireContext().applicationContext as SherlockApplication).accountsRepository,
            (requireContext().applicationContext as SherlockApplication).encryptionService
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditBinding.inflate(inflater, container, false).apply {
            this.viewModel = addEditViewModel
        }

        binding.lifecycleOwner = this.viewLifecycleOwner

        addEditViewModel.start(args.accountId)

        addEditViewModel.navigateToHomeScreen.observe(viewLifecycleOwner, { navigate ->
            if (navigate) {
                findNavController().navigate(AddEditFragmentDirections.actionAddEditFragmentToHomeFragment())
                addEditViewModel.doneNavigating()
            }
        })

        addEditViewModel.snackbarText.observe(viewLifecycleOwner, { string ->
            if (string != null) {
                Snackbar.make(requireView(), getString(string), Snackbar.LENGTH_SHORT).show()
                addEditViewModel.doneShowingSnackbar()
            }
        })

        binding.toggleViewPasswordButton.setOnClickListener {
            if (toggle) {
                binding.toggleViewPasswordButton.setImageResource(R.drawable.ic_outline_remove_red_eye_24)
                binding.passwordEditText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.passwordEditText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.toggleViewPasswordButton.setImageResource(R.drawable.ic_baseline_remove_red_eye_24)
            }
            toggle = !toggle
        }

        return binding.root
    }
}