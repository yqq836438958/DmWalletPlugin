#!/bin/bash
set -x
APK_UPLOAD_DEBUG=$1
package_name=$2
OTA_DOWNLOAD_URL=$3
REMOTE_UPLOAD_URL=$4
SQL_UPLOAD_URL=$5
soft_name=$6
min_version=$7
max_version=$8
rdm_job_id=$9
function begin_apk_upgrade()
{
	WORKSPACE="."
	ROOTPATH=${WORKSPACE}
	TEMPPATH=${WORKSPACE}/temp/unzipfile

	if [ ! -d "${TEMPPATH}" ];then
       		mkdir ${TEMPPATH} -p
	fi

	WORKPATH=${WORKSPACE}
	if [ ${APK_UPLOAD_DEBUG} -eq 1 ]
	then
		PACKAGEPATH=${WORKSPACE}/bin/*_debug_internal.apk
	else
		PACKAGEPATH=${WORKSPACE}/bin/*_release_internal.apk
	fi
	
	create_time=`date +%Y-%m-%d" "%H:%M:%S`
	compile_time=${create_time}
	
	TOPATH=${WORKSPACE}/bin/apkupload

	if [ ! -d "${TOPATH}" ];then
       		mkdir ${TOPATH} -p
	fi

	unzip -o ${PACKAGEPATH} -d ${TEMPPATH}
	
	version_code=`grep BUILD_APP_VN_DAY= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`
	build_number=`grep BUILD_APP_BN_BUILD_NO= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`
	
	channel_id=`grep CHANNEL= ${TEMPPATH}/assets/channel.ini | cut -d'=' -f2`
	build_id=`grep BUILD_APP_BN_BUILD_NO= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`
	BUILD_APP_SN_FLG=`grep BUILD_APP_SN_FLG= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`
	BUILD_APP_SN_VER=`grep BUILD_APP_SN_VER= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`
	BUILD_APP_SN_PUBLISH_TYPE=`grep BUILD_APP_SN_PUBLISH_TYPE= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`
	
	version_name=${BUILD_APP_SN_VER}.${version_code}.${build_number}
	
	version_code_full=${BUILD_APP_SN_VER}${version_code}
	LC=`grep LC= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`
	LCID=`grep LCID= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`
	qua_header=${BUILD_APP_SN_FLG}
	echo BUILD_APP_SN_FLG :${BUILD_APP_SN_FLG}
	echo BUILD_APP_SN_VER: ${BUILD_APP_SN_VER}
	echo BUILD_APP_SN_PUBLISH_TYPE:${BUILD_APP_SN_PUBLISH_TYPE}
	echo qua_header:${qua_header}
	#soft_name=${qua_header:0:8}
	create_user=rdm
	version_type=`grep BUILD_APP_SN_PUBLISH_TYPE= ${TEMPPATH}/assets/build_config.ini | cut -d'=' -f2`

	file_name=`basename ${PACKAGEPATH}`
	base_name=`basename ${PACKAGEPATH} .apk`
	
	full_package_size=$(stat -Lc %s ${PACKAGEPATH})
	full_package_md5=$(md5sum ${PACKAGEPATH} | awk '{print $1}')

	full_package_url=$(printf ${OTA_DOWNLOAD_URL} ${full_package_md5} ${file_name})
	
	UPDATE_SQL=${TOPATH}/${base_name}.sql
	UPDATE_APK=${TOPATH}/${file_name}
	pack_sql="insert into t_comm_app_package (package_name, version_code, version_name, file_name, qua_header, soft_name,min_version,max_version, LC, LCID, full_package_url, full_package_size, full_package_md5, build_id, channel_id, version_type, create_user, compile_time, create_time,rdm_job_id,rdm_build_id,app_type) values (\"${package_name}\", \"${version_code_full}\", \"${version_name}\", \"${file_name}\", \"${qua_header}\", \"${soft_name}\",\"${min_version}\",\"${max_version}\", \"${LC}\", \"${LCID}\", \"${full_package_url}\", \"${full_package_size}\", \"${full_package_md5}\", \"${build_id}\", \"${channel_id}\", \"${version_type}\", \"${create_user}\", \"${compile_time}\", \"${create_time}\", \"${rdm_job_id}\", ${build_id}, 1);"

	echo ${pack_sql} > ${UPDATE_SQL}

	cp ${PACKAGEPATH} ${TOPATH}/

	rm ${TEMPPATH} -rf
	
	echo ${REMOTE_UPLOAD_URL}
	echo ${SQL_UPLOAD_URL}
	curl -F "action=upload" -F "filename=@${UPDATE_APK}" ${REMOTE_UPLOAD_URL}
	curl -F "action=upload" -F "filename=@${UPDATE_SQL}" ${SQL_UPLOAD_URL}
}

begin_apk_upgrade
