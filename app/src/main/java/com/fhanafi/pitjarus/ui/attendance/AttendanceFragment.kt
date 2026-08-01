package com.fhanafi.pitjarus.ui.attendance

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fhanafi.pitjarus.R
import com.fhanafi.pitjarus.data.repository.AttendanceType
import com.fhanafi.pitjarus.databinding.FragmentAttendanceBinding
import com.fhanafi.pitjarus.utils.UiState
import com.fhanafi.pitjarus.utils.hideGlobalLoading
import com.fhanafi.pitjarus.utils.ImageCompressor
import com.fhanafi.pitjarus.utils.showGlobalLoading
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AttendanceFragment : Fragment() {
    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: AttendanceViewModel by viewModels()

    private var pendingType: AttendanceType? = null
    private var pendingCameraFile: File? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingType?.let(::showImageSourceChooser)
        } else {
            Toast.makeText(requireContext(), "Izin lokasi dibutuhkan untuk attendance", Toast.LENGTH_LONG).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Izin kamera dibutuhkan untuk mengambil foto", Toast.LENGTH_LONG).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            submitSelectedImage(uri)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null) {
            submitImageFile(file)
        } else {
            Toast.makeText(requireContext(), "Foto tidak dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindCurrentDateTime()
        observeState()
        binding.buttonCheckIn.setOnClickListener {
            requestAttendancePhoto(AttendanceType.CHECK_IN)
        }
        binding.buttonContinueWorking.setOnClickListener {
            findNavController().navigate(AttendanceFragmentDirections.actionAttendanceFragmentToStoreListFragment())
        }
        binding.buttonCheckOut.setOnClickListener {
            requestAttendancePhoto(AttendanceType.CHECK_OUT)
        }
    }

    private fun bindCurrentDateTime() = with(binding) {
        val now = Date()
        textDate.text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(now)
        textTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        val loading = state.submitState is UiState.Loading
                        updateButtons(state.session.checkedIn, loading)
                        binding.textStatus.text = getString(
                            if (state.session.checkedIn) R.string.checked_in else R.string.not_checked_in
                        )
                        binding.textStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                if (state.session.checkedIn) R.color.success else R.color.error
                            )
                        )
                        if (loading) {
                            showGlobalLoading(state.loadingMessage ?: getString(R.string.loading))
                        } else {
                            hideGlobalLoading()
                        }
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AttendanceEvent.Success -> {
                                Snackbar.make(binding.root, R.string.attendance_saved, Snackbar.LENGTH_SHORT).show()
                                if (event.type == AttendanceType.CHECK_IN) {
                                    findNavController().navigate(AttendanceFragmentDirections.actionAttendanceFragmentToStoreListFragment())
                                }
                            }
                            is AttendanceEvent.Error -> Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                            is AttendanceEvent.Unauthorized -> Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateButtons(checkedIn: Boolean, loading: Boolean) {
        binding.buttonCheckIn.isEnabled = !loading && !checkedIn
        binding.buttonContinueWorking.visibility = if (checkedIn) View.VISIBLE else View.GONE
        binding.buttonContinueWorking.isEnabled = !loading && checkedIn
        binding.buttonCheckOut.isEnabled = !loading && checkedIn
    }

    private fun requestAttendancePhoto(type: AttendanceType) {
        pendingType = type
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        showImageSourceChooser(type)
    }

    private fun showImageSourceChooser(type: AttendanceType) {
        pendingType = type
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_attendance_photo)
            .setItems(
                arrayOf(
                    getString(R.string.take_photo),
                    getString(R.string.choose_from_gallery)
                )
            ) { _, which ->
                when (which) {
                    0 -> requestCamera()
                    1 -> pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }
            .show()
    }

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("attendance_", ".jpg", requireContext().cacheDir)
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        pendingCameraFile = photoFile
        takePictureLauncher.launch(uri)
    }

    private fun submitSelectedImage(uri: Uri) {
        val file = copyUriToTempFile(uri)
        if (file == null) {
            Toast.makeText(requireContext(), "Gambar gagal dibaca", Toast.LENGTH_SHORT).show()
            return
        }
        submitImageFile(file)
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        return runCatching {
            val file = File.createTempFile("attendance_picker_", ".jpg", requireContext().cacheDir)
            requireContext().contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "Input stream kosong" }
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file
        }.getOrElse { exception ->
            Timber.e(exception, "Failed to copy selected image")
            null
        }
    }

    private fun submitImageFile(photoFile: File) {
        val type = pendingType ?: return
        val location = getLastKnownLocation()
        if (location == null) {
            Toast.makeText(requireContext(), "Lokasi GPS belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        val compressedPhoto = runCatching {
            ImageCompressor.compressJpegToMaxSize(photoFile, maxSizeKb = 100)
        }.getOrElse { exception ->
            Timber.e(exception, "Image compression failed")
            Toast.makeText(requireContext(), "Kompresi foto gagal", Toast.LENGTH_SHORT).show()
            return
        }
        Timber.d("Attendance photo compressed to ${compressedPhoto.size / 1024}KB")
        val photoBase64 = Base64.encodeToString(compressedPhoto, Base64.NO_WRAP)
        viewModel.submit(
            type = type,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            photoFileName = photoFile.name,
            photoBase64 = photoBase64
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLastKnownLocation(): Location? {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!hasLocationPermission()) return null
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
