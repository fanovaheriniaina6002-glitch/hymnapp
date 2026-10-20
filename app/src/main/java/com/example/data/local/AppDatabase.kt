package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SongEntity::class,
        CategoryEntity::class,
        UserProfileEntity::class,
        ArchivePhotoEntity::class,
        ScheduleItemEntity::class,
        TraktaEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            HymnCatalog.ensureInitialized(context.applicationContext)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jesosy_mamonjy_daoud_offline.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                CoroutineScope(Dispatchers.IO).launch {
                    seedIfEmpty(instance.songDao())
                }
                instance
            }
        }

        private suspend fun seedIfEmpty(dao: SongDao) {
            if (dao.getTraktaCount() == 0) {
                val defaultTrakta = listOf(
                    TraktaEntity(
                        id = 1,
                        numberLabel = "N° 01",
                        title = "Tena Misy ny Lanitra",
                        author = "M. A. DAOUD",
                        coverKey = "trakta_1",
                        coverBgHex = "#351019",
                        content = """
                            JAONA 14:1-3
                            « Aza malahelo ny fonareo; minoa an'Andriamanitra, ary minoa Ahy koa. Ao an-tranon'ny Raiko misy fitoerana maro; raha tsy izany, dia efa nilaza taminareo Aho; fa handeha hamboatra fitoerana ho anareo Aho. »

                            1. NY FAMPANANTENAN'I JESOSY
                            Tena misy tokoa ny Lanitra! Tsy angano na nofinofy izany fa fitoerana masina sy mandrakizay namboarin'Andriamanitra ho an'izay rehetra mino sy mandray an'i Jesosy Kristy ho Tompo sy Mpamonjy.

                            2. TOERANA TSY MISY FAHORIANA INTSONY
                            Ao amin'ny Apokalypsy 21:4 dia voasoratra hoe: « Ary hofafany ny ranomaso rehetra amin'ny masony; ary tsy hisy fahafatesana intsony, sady tsy hisy alahelo, na fitarainana, na fanaintainana. »

                            3. AHOANA NO AHATONGAVANA ANY?
                            Hoy Jesosy: « Izaho no lalana sy fahamarinana ary fiainana; tsy misy olona mankany amin'ny Ray, afa-tsy amin'ny alalako. » (Jaona 14:6). Mibebaha amin'ny fahotanao anio, raiso ny famelan-keloka amin'ny Ràn'i Jesosy, dia ho voasoratra ao amin'ny Bokin'ny Fiainana ny anaranao.
                        """.trimIndent(),
                        isOffline = true
                    ),
                    TraktaEntity(
                        id = 2,
                        numberLabel = "N° 02",
                        title = "Tafasokatra ve ny Vavahadin'ny Helo ?",
                        author = "M. A. DAOUD",
                        coverKey = "trakta_2",
                        coverBgHex = "#2C1810",
                        content = """
                            LIOKA 16:19-31 • HEBREO 9:27
                            « Ary tahaka ny nanendrena ny olona ho faty indray mandeha, ary rehefa afaka izany, dia hisy fitsarana. »

                            1. FAMPITANDREMANA AM-PITIAVANA
                            Ny Tenin'Andriamanitra dia manambara mazava fa misy lalana roa eooloan'ny zanak'olombelona: ny lalana tery mankany amin'ny fiainana mandrakizay, ary ny lalana malalaka mankany amin'ny fahaverezana.

                            2. JESOSY NO MPAMONJY MAHERY
                            Tsy sitrak'Andriamanitra ny hahavery na dia iray aza, fa ny mbaตระหนัก sy hibebahan'ny rehetra handraisana ny famonjena. Natsangana tamin'ny maty Jesosy Kristy ary manana ny fanalahidin'ny fahafatesana sy ny fiainan-tsy hita.

                            3. ANIO NO ANDRO FAMONJENA
                            Aza mangataka andro! Antsoy ny anaran'ny Tompo Jesosy izao ankehitriny izao, fa Izy no manafaka amin'ny fanamelohana rehetra ary manome fiadanana sy fiainana vaovao.
                        """.trimIndent(),
                        isOffline = true
                    ),
                    TraktaEntity(
                        id = 3,
                        numberLabel = "N° 03",
                        title = "Soa Ihany",
                        author = "M. A. DAOUD",
                        coverKey = "trakta_3",
                        coverBgHex = "#1A1A24",
                        content = """
                            ROMANA 8:28 • SALAMO 103:1-5
                            « Ary fantatsika fa ny zavatra rehetra dia miara-miasa hahasoa izay tia an'Andriamanitra, dia izay voantso araka ny fikasany. »

                            1. SOA IHANY FA AO JESOSY
                            Na inona na inona onjam-piainana lalovanao, soa ihany fa misy Mpamonjy tia sy mahery izay tsy mba mandao antsika! Ny famindram-pon'Andriamanitra dia vaovao isa-maraina.

                            2. MPANASITRANA SY MPAMPIONONA
                            Izy no mamela ny helokao rehetra, manasitrana ny aretinao rehetra, ary manavotra ny ainao tsy ho latsaka an-davaka. Ao amin'i Jesosy dia misy fanantenana velona mandrakariva.

                            3. MATOKIA AZY AMIN'NY FONAO MANONTOLO
                            Apetraho eo am-pelatanan'ny Tompo ny fiainanao sy ny ankohonanao, fa Izy no miahy anao sy mitahy anao mandrakizay.
                        """.trimIndent(),
                        isOffline = true
                    ),
                    TraktaEntity(
                        id = 4,
                        numberLabel = "N° 04",
                        title = "Paska Iray...",
                        author = "M. A. DAOUD",
                        coverKey = "trakta_4",
                        coverBgHex = "#28181A",
                        content = """
                            1 KORINTIANA 5:7 • ISAIA 53:4-5
                            « Fa voavono Kristy, Paska ho antsika. »

                            1. ILAY ZANAK'ONDRIN'ANDRIAMANITRA
                            Jesosy Kristy no Ilay Paska marina, Zanak'ondrin'Andriamanitra izay nanaisotra ny fahotan'izao tontolo izao teo amin'ny hazofijaliana tao Kalvary.

                            2. NY RÀ SAROBIDY MANADIO SY MIARO
                            Tahaka ny nanavotana ny Zanak'Isiraely tamin'ny alalan'ny ràn'ny zanak'ondry, dia ny Ràn'i Jesosy Kristy irery ihany no manadio ny fo sy miaro ny mino amin'ny fahaverezana.

                            3. NY HERIN'NY FITSANGANANA AMIN'NY MATY
                            Tsy nijanona tao am-pasana Jesosy fa nitsangana tamin'ny maty tamin'ny hery sy voninahitra! Velona Izy anio mba hanome anao fandresena, fanasitranana ary ny Fanahy Masina.
                        """.trimIndent(),
                        isOffline = true
                    )
                )
                dao.insertTraktaItems(defaultTrakta)
            }

            if (dao.getScheduleItemCount() == 0) {
                val defaultSchedule = listOf(
                    ScheduleItemEntity(
                        id = 1,
                        groupType = "ALAHADY",
                        title = "Sekoly Alahady & Tanora",
                        subtitle = "Fianarana Soratra Masina",
                        timeRange = "08h30 – 09h30",
                        details = "Fotoana fianarana Soratra Masina ho an'ny ankizy, ny zatovo ary ny tanora rehetra ao amin'ny Fiangonana Jesosy Mamonjy M. A. Daoud.",
                        isHighlighted = false
                    ),
                    ScheduleItemEntity(
                        id = 2,
                        groupType = "ALAHADY",
                        title = "Maraina",
                        subtitle = "Hira, Vavaka sy Toriteny",
                        timeRange = "09h30 – 11h00",
                        details = "Fanompoam-pivavahana lehibe Alahady maraina: Fiderana sy fihirana, vavaka fifonana sy fisaorana, ary fitoriana ny Tenin'Andriamanitra.",
                        isHighlighted = true
                    ),
                    ScheduleItemEntity(
                        id = 3,
                        groupType = "ALAHADY",
                        title = "Hariva",
                        subtitle = "Hira, Vavaka sy Toriteny",
                        timeRange = "14h00 – 15h00",
                        details = "Fanompoam-pivavahana Alahady hariva: Hira fiderana, vavaka fangatahana ny Fanahy Masina sy fanasitranana, ary toriteny.",
                        isHighlighted = false
                    ),
                    ScheduleItemEntity(
                        id = 4,
                        groupType = "ANDAVANANDRO",
                        title = "Vavaka Maraina",
                        subtitle = "Talata – Zoma",
                        timeRange = "05h30 – 06h00",
                        details = "Fotoam-bavaka maraina isan'andro (Talata hatramin'ny Zoma) hitondrana am-bavaka ny fiangonana, ny ankohonana ary ny firenena.",
                        isHighlighted = false
                    ),
                    ScheduleItemEntity(
                        id = 5,
                        groupType = "ANDAVANANDRO",
                        title = "Fivoriana",
                        subtitle = "Alarobia hariva",
                        timeRange = "18h00 – 19h30",
                        details = "Fivoriana sy fampianarana Soratra Masina isaky ny Alarobia hariva hampitomboana ny finoana sy ny fahalalana ny Tenin'ny Tompo.",
                        isHighlighted = false
                    )
                )
                dao.insertScheduleItems(defaultSchedule)
            }

            if (dao.getArchivePhotoCount() == 0) {
                val defaultArchivePhotos = listOf(
                    ArchivePhotoEntity(
                        id = 1,
                        title = "Fihaonambe 1965",
                        subtitle = "Ankadembahoaka • Foibe",
                        year = "Tahirintsary 1965",
                        location = "Ankadembahoaka • Foibe",
                        category = "TANTARA",
                        description = "Vavolombelon'ny fitahian'ny Tompo sy ny firoborobon'ny asa fitoriana ny Filazantsara teto Madagasikara. Toerana masina fivorian'ny mpino an-jatony.",
                        drawableKey = "archive_1",
                        isFeatured = true
                    ),
                    ArchivePhotoEntity(
                        id = 2,
                        title = "Mpitandrina M. A. Daoud",
                        subtitle = "Mpanorina ny asa",
                        year = "Tahirintsary 1963",
                        location = "Mpanorina ny asa • Antananarivo",
                        category = "MPITANDRINA",
                        description = "Ny Mpanompon'Andriamanitra M. A. Daoud sy Ramatoa vadiny, izay nanokana ny fiainany manontolo hitory ny Filazantsara sy hanorina ny Fiangonana Jesosy Mamonjy teto Madagasikara.",
                        drawableKey = "archive_2",
                        isFeatured = false
                    ),
                    ArchivePhotoEntity(
                        id = 3,
                        title = "Iraka eran-tany",
                        subtitle = "Tantaran'ny Fiangonana",
                        year = "Tahirintsary 1967",
                        location = "Tantaran'ny Fiangonana • Madagasikara",
                        category = "TANTARA",
                        description = "Fampielezana ny Tenin'Andriamanitra sy ny vina fitoriana ny Filazantsara manerana ny Nosy sy eran-tany tamin'ny herin'ny Fanahy Masina.",
                        drawableKey = "archive_3",
                        isFeatured = false
                    ),
                    ArchivePhotoEntity(
                        id = 4,
                        title = "Alitara Masina",
                        subtitle = "Tempoly Foibe",
                        year = "Tahirintsary 1968",
                        location = "Tempoly Foibe • Antananarivo",
                        category = "FIANGONANA",
                        description = "Vavolombelon'ny fitahian'ny Tompo sy ny firoborobon'ny asa fitoriana ny Filazantsara teto Madagasikara. Toerana masina fivorian'ny mpino an-jatony.",
                        drawableKey = "archive_4",
                        isFeatured = false
                    ),
                    ArchivePhotoEntity(
                        id = 5,
                        title = "Tempoly Ankadembahoaka",
                        subtitle = "Vavaka sy Fiderana",
                        year = "Tahirintsary 1975",
                        location = "Niorina 1975 • Antananarivo",
                        category = "FIANGONANA",
                        description = "Vavolombelon'ny fitahian'ny Tompo sy ny firoborobon'ny asa fitoriana ny Filazantsara teto Madagasikara. Toerana masina fivorian'ny mpino an-jatony.",
                        drawableKey = "archive_5",
                        isFeatured = false
                    ),
                    ArchivePhotoEntity(
                        id = 6,
                        title = "Vahoakan'Andriamanitra",
                        subtitle = "Fiaraha-mivavaka",
                        year = "Tahirintsary 1980",
                        location = "Fiaraha-mivavaka • Madagasikara",
                        category = "FOTOANA",
                        description = "Fiaraha-mivavaka sy fifaliana lehibe teo amin'ny vahoakan'Andriamanitra nivory hankalaza sy hidera ny anaran'i Jesosy Mamonjy.",
                        drawableKey = "archive_6",
                        isFeatured = false
                    )
                )
                dao.insertArchivePhotos(defaultArchivePhotos)
            }

            if (dao.getUserProfileCount() == 0) {
                dao.saveUserProfile(
                    UserProfileEntity(
                        id = 1,
                        displayName = "Frère Aina",
                        gender = "male",
                        region = "Ambohipo foibe",
                        themeMode = "jour",
                        fontSizeSp = 18,
                        photoUri = "",
                        isOnboardingCompleted = false
                    )
                )
            }

            val defaultCategories = listOf(
                CategoryEntity("1_trakta", "Trakta", "13 boky", 13),
                CategoryEntity("2_fotoana", "Fotoana", "Ora fiangonana", 5),
                CategoryEntity("3_rakitsary", "Rakitsary", "Sary tahiry", 41)
            )
            dao.insertCategories(defaultCategories)
        }
    }
}
