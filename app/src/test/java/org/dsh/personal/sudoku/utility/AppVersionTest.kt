package org.dsh.personal.sudoku.utility

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class AppVersionTest {
    private val mockContext: Context = mockk()
    private val mockPackageManager: PackageManager = mockk()
    private val mockPackageInfo: PackageInfo = mockk(relaxed = true)

    private val testPackageName = "org.dsh.personal.sudoku"
    private val defaultVersionName = "N/A"

    @Test
    fun `getAppVersionName returns version name when found`() {
        val expectedVersion = "1.0.0"
        every { mockContext.packageName } returns testPackageName
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(testPackageName, 0) } returns mockPackageInfo
        mockPackageInfo.versionName = expectedVersion

        val versionName = getAppVersionName(mockContext)

        Assert.assertEquals(expectedVersion, versionName)
    }

    @Test
    fun `getAppVersionName returns default when NameNotFoundException occurs`() {
        every { mockContext.packageName } returns testPackageName
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(testPackageName, 0) } throws PackageManager.NameNotFoundException()

        val versionName = getAppVersionName(mockContext)

        Assert.assertEquals(defaultVersionName, versionName)
    }

    @Test
    fun `getAppVersionName returns custom default when NameNotFoundException occurs`() {
        val customDefault = "0.0.0"
        every { mockContext.packageName } returns testPackageName
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(testPackageName, 0) } throws PackageManager.NameNotFoundException()

        val versionName = getAppVersionName(mockContext, default = customDefault)

        Assert.assertEquals(customDefault, versionName)
    }

    @Test
    fun `getAppVersionName returns default when packageInfo is null`() {
        // This scenario assumes getPackageInfo can return null, though typically it throws NameNotFoundException.
        // If the API contract guarantees it throws, this test might be for a hypothetical case.
        every { mockContext.packageName } returns testPackageName
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(testPackageName, 0) } returns null // PackageInfo itself is null

        val versionName = getAppVersionName(mockContext)

        Assert.assertEquals(defaultVersionName, versionName)
    }

    @Test
    fun `getAppVersionName returns default when versionName in packageInfo is null`() {
        every { mockContext.packageName } returns testPackageName
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(testPackageName, 0) } returns mockPackageInfo
        mockPackageInfo.versionName = null // versionName field is null

        val versionName = getAppVersionName(mockContext)

        Assert.assertEquals(defaultVersionName, versionName)
    }

    @Test
    fun `getAppVersionName returns custom default when versionName in packageInfo is null`() {
        val customDefault = "Unavailable"
        every { mockContext.packageName } returns testPackageName
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(testPackageName, 0) } returns mockPackageInfo
        mockPackageInfo.versionName = null

        val versionName = getAppVersionName(mockContext, default = customDefault)

        Assert.assertEquals(customDefault, versionName)
    }
}