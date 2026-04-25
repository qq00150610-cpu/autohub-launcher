package com.autocar.launcher.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.autocar.launcher.util.LogUtil

/**
 * 应用数据 Provider
 * 提供应用数据访问接口
 */
class AppDataProvider : ContentProvider() {

    companion object {
        private const val TAG = "AppDataProvider"
        
        const val AUTHORITY = "com.autocar.launcher.provider"
        
        // URI Matcher Code
        private const val APPS = 1
        private const val APP_DETAIL = 2
        
        // Column Names
        const val COLUMN_PACKAGE_NAME = "package_name"
        const val COLUMN_APP_NAME = "app_name"
        const val COLUMN_IS_LAUNCHER = "is_launcher"
        const val COLUMN_INSTALL_TIME = "install_time"
        const val COLUMN_UPDATE_TIME = "update_time"
        
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/apps")
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "apps", APPS)
            addURI(AUTHORITY, "apps/*", APP_DETAIL)
        }
    }
    
    override fun onCreate(): Boolean {
        LogUtil.d(TAG, "Provider 创建")
        return true
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            APPS -> queryApps()
            APP_DETAIL -> queryAppDetail(uri.lastPathSegment)
            else -> null
        }
    }
    
    /**
     * 查询所有应用
     */
    private fun queryApps(): Cursor {
        val cursor = MatrixCursor(arrayOf(
            COLUMN_PACKAGE_NAME,
            COLUMN_APP_NAME,
            COLUMN_IS_LAUNCHER,
            COLUMN_INSTALL_TIME,
            COLUMN_UPDATE_TIME
        ))
        
        // TODO: 从数据库或缓存加载应用数据
        
        return cursor
    }
    
    /**
     * 查询单个应用详情
     */
    private fun queryAppDetail(packageName: String?): Cursor {
        val cursor = MatrixCursor(arrayOf(
            COLUMN_PACKAGE_NAME,
            COLUMN_APP_NAME,
            COLUMN_IS_LAUNCHER,
            COLUMN_INSTALL_TIME,
            COLUMN_UPDATE_TIME
        ))
        
        // TODO: 查询指定应用详情
        
        return cursor
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        LogUtil.d(TAG, "插入数据: $uri")
        return null
    }
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        LogUtil.d(TAG, "更新数据: $uri")
        return 0
    }
    
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        LogUtil.d(TAG, "删除数据: $uri")
        return 0
    }
    
    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            APPS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.apps"
            APP_DETAIL -> "vnd.android.cursor.item/vnd.$AUTHORITY.app"
            else -> ""
        }
    }
}
