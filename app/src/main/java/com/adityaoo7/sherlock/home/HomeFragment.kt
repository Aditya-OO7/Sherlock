package com.adityaoo7.sherlock.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.authentication.AuthenticationActivity
import com.adityaoo7.sherlock.data.LoginAccount
import com.adityaoo7.sherlock.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding

    private val homeViewModel by viewModels<HomeViewModel> {
        HomeViewModelFactory(
            (requireContext().applicationContext as SherlockApplication).accountsRepository,
            (requireContext().applicationContext as SherlockApplication).encryptionService
        )
    }

    private lateinit var listAdapter: AccountsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
            .apply {
                this.viewModel = homeViewModel
            }

        binding.lifecycleOwner = this.viewLifecycleOwner

        homeViewModel.dataLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                binding.loadingAccountsLayout.visibility = View.VISIBLE
                binding.accountsContainerLayout.visibility = View.GONE
            } else {
                binding.loadingAccountsLayout.visibility = View.GONE
                binding.accountsContainerLayout.visibility = View.VISIBLE
            }
        })

        homeViewModel.empty.observe(viewLifecycleOwner, { isEmpty ->
            if (isEmpty != null) {
                if (isEmpty) {
                    binding.accountsListLayout.visibility = View.GONE
                    binding.noAccountsLayout.visibility = View.VISIBLE
                } else {
                    binding.accountsListLayout.visibility = View.VISIBLE
                    binding.noAccountsLayout.visibility = View.GONE
                }
            }
        })

        homeViewModel.snackbarText.observe(viewLifecycleOwner, { string ->
            if (string != null) {
                Snackbar.make(requireView(), getString(string), Snackbar.LENGTH_LONG).show()
                homeViewModel.doneShowingSnackbar()
            }
        })

        homeViewModel.createNewAccount.observe(viewLifecycleOwner, { navigateToAdd ->
            if (navigateToAdd) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAddEditFragment(
                        null
                    )
                )
                homeViewModel.doneCreatingNewAccount()
            }
        })

        homeViewModel.openExistingAccount.observe(viewLifecycleOwner, { accountId ->
            if (accountId != null) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAccountDetailFragment(
                        accountId
                    )
                )
                homeViewModel.doneOpeningExistingAccount()
            }
        })


        binding.addAccountFab.setOnClickListener {
            homeViewModel.addNewAccount()
        }

        // TODO: For testing purpose deleting auth data and navigating to auth screen

        binding.gotoAuthButton.setOnClickListener {
            val manager =
                (requireContext().applicationContext as SherlockApplication).sharedPreferencesManager
            manager.putVerificationAccount(LoginAccount())
            manager.putSalt("")
            manager.putIsRegistered(false)
            startActivity(Intent(requireContext(), AuthenticationActivity::class.java))
        }

        val viewModel = binding.viewModel

        if (viewModel != null) {
            listAdapter = AccountsAdapter(viewModel)

            binding.accountsList.adapter = listAdapter
        } else {
            Log.d("Home Fragment", "ViewModel not initialized")
        }

        return binding.root
    }

}
