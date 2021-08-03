package com.adityaoo7.sherlock.detail

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.adityaoo7.sherlock.R
import com.adityaoo7.sherlock.SherlockApplication
import com.adityaoo7.sherlock.databinding.FragmentAccountDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis

class AccountDetailFragment : Fragment() {

    private lateinit var binding: FragmentAccountDetailBinding

    private val detailViewModel by viewModels<AccountDetailViewModel> {
        AccountDetailViewModelFactory(
            (requireContext().applicationContext as SherlockApplication).accountsRepository,
            (requireContext().applicationContext as SherlockApplication).encryptionService
        )
    }

    private val args: AccountDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(R.integer.material_motion_duration_long_1).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().getColor(R.color.design_default_color_surface))
        }

        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.material_motion_duration_long_1).toLong()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
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
                findNavController().popBackStack(R.id.accountDetailFragment, true)
                detailViewModel.doneDeletingAccount()
            }
        })

        val clipBoard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        binding.userNameCopyButton.setOnClickListener {
            val clip: ClipData =
                ClipData.newPlainText("simple text", detailViewModel.account.value?.userName)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(
                requireContext(),
                getString(R.string.user_name_copied_toast),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.passwordCopyButton.setOnClickListener {
            val clip: ClipData =
                ClipData.newPlainText("simple text", detailViewModel.account.value?.password)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(
                requireContext(),
                getString(R.string.password_copied_toast),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.openUrlButton.setOnClickListener {
            val webpage: Uri = Uri.parse(detailViewModel.account.value?.uri)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            }
        }

        return binding.root
    }
}