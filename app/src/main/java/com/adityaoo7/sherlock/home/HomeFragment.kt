package com.adityaoo7.sherlock.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private val TAG = HomeFragment::class.java.simpleName

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

        homeViewModel.resetPassword.observe(viewLifecycleOwner, { navigateToResetPassword ->
            if (navigateToResetPassword) {
                homeViewModel.doneNavigatingResetPassword()
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToResetPasswordFragment()
                )
            }
        })


        binding.addAccountFab.setOnClickListener {
            homeViewModel.addNewAccount()
        }

        val viewModel = binding.viewModel

        if (viewModel != null) {
            listAdapter = AccountsAdapter(viewModel)

            binding.accountsList.adapter = listAdapter
        } else {
            Log.d(TAG, "ViewModel not initialized")
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.reset_password -> {
            homeViewModel.resetPassword()
            true
        }
        else -> false
    }

}
