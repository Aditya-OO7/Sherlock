package com.adityaoo7.sherlock.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.databinding.FragmentAccountDetailBinding
import com.google.android.material.snackbar.Snackbar

class AccountDetailFragment : Fragment() {

    private lateinit var binding: FragmentAccountDetailBinding

    private val detailViewModel by viewModels<AccountDetailViewModel> {
        AccountDetailViewModelFactory(
            (requireContext().applicationContext as SherlockApplication).accountsRepository,
            (requireContext().applicationContext as SherlockApplication).encryptionService
        )
    }

    private val args: AccountDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAccountDetailBinding.inflate(inflater, container, false).apply {
            this.viewModel = detailViewModel
        }

        binding.lifecycleOwner = this.viewLifecycleOwner

        detailViewModel.start(args.accountId)

        detailViewModel.snackbarText.observe(viewLifecycleOwner, { string ->
            if (string != null) {
                Snackbar.make(requireView(), getString(string), Snackbar.LENGTH_SHORT).show()
                detailViewModel.doneShowingSnackbar()
            }
        })

        detailViewModel.editAccount.observe(viewLifecycleOwner, { navigateToEdit ->
            if (navigateToEdit) {
                findNavController().navigate(
                    AccountDetailFragmentDirections.actionAccountDetailFragmentToAddEditFragment(
                        args.accountId
                    )
                )
                detailViewModel.doneNavigatingEditScreen()
            }
        })

        detailViewModel.deleteAccount.observe(viewLifecycleOwner, { deleteAndNavigateToHome ->
            if (deleteAndNavigateToHome) {
                findNavController().navigate(
                    AccountDetailFragmentDirections.actionAccountDetailFragmentToHomeFragment()
                )
                detailViewModel.doneDeletingAccount()
            }
        })

        return binding.root
    }
}