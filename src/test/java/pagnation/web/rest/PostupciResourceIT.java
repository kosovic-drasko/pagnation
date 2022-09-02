package pagnation.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pagnation.IntegrationTest;
import pagnation.domain.Postupci;
import pagnation.repository.PostupciRepository;

/**
 * Integration tests for the {@link PostupciResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PostupciResourceIT {

    private static final Integer DEFAULT_BROJ = 1;
    private static final Integer UPDATED_BROJ = 2;

    private static final String DEFAULT_IME = "AAAAAAAAAA";
    private static final String UPDATED_IME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/postupcis";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PostupciRepository postupciRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPostupciMockMvc;

    private Postupci postupci;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Postupci createEntity(EntityManager em) {
        Postupci postupci = new Postupci().broj(DEFAULT_BROJ).ime(DEFAULT_IME);
        return postupci;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Postupci createUpdatedEntity(EntityManager em) {
        Postupci postupci = new Postupci().broj(UPDATED_BROJ).ime(UPDATED_IME);
        return postupci;
    }

    @BeforeEach
    public void initTest() {
        postupci = createEntity(em);
    }

    @Test
    @Transactional
    void createPostupci() throws Exception {
        int databaseSizeBeforeCreate = postupciRepository.findAll().size();
        // Create the Postupci
        restPostupciMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(postupci)))
            .andExpect(status().isCreated());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeCreate + 1);
        Postupci testPostupci = postupciList.get(postupciList.size() - 1);
        assertThat(testPostupci.getBroj()).isEqualTo(DEFAULT_BROJ);
        assertThat(testPostupci.getIme()).isEqualTo(DEFAULT_IME);
    }

    @Test
    @Transactional
    void createPostupciWithExistingId() throws Exception {
        // Create the Postupci with an existing ID
        postupci.setId(1L);

        int databaseSizeBeforeCreate = postupciRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPostupciMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(postupci)))
            .andExpect(status().isBadRequest());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllPostupcis() throws Exception {
        // Initialize the database
        postupciRepository.saveAndFlush(postupci);

        // Get all the postupciList
        restPostupciMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(postupci.getId().intValue())))
            .andExpect(jsonPath("$.[*].broj").value(hasItem(DEFAULT_BROJ)))
            .andExpect(jsonPath("$.[*].ime").value(hasItem(DEFAULT_IME)));
    }

    @Test
    @Transactional
    void getPostupci() throws Exception {
        // Initialize the database
        postupciRepository.saveAndFlush(postupci);

        // Get the postupci
        restPostupciMockMvc
            .perform(get(ENTITY_API_URL_ID, postupci.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(postupci.getId().intValue()))
            .andExpect(jsonPath("$.broj").value(DEFAULT_BROJ))
            .andExpect(jsonPath("$.ime").value(DEFAULT_IME));
    }

    @Test
    @Transactional
    void getNonExistingPostupci() throws Exception {
        // Get the postupci
        restPostupciMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPostupci() throws Exception {
        // Initialize the database
        postupciRepository.saveAndFlush(postupci);

        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();

        // Update the postupci
        Postupci updatedPostupci = postupciRepository.findById(postupci.getId()).get();
        // Disconnect from session so that the updates on updatedPostupci are not directly saved in db
        em.detach(updatedPostupci);
        updatedPostupci.broj(UPDATED_BROJ).ime(UPDATED_IME);

        restPostupciMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPostupci.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPostupci))
            )
            .andExpect(status().isOk());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
        Postupci testPostupci = postupciList.get(postupciList.size() - 1);
        assertThat(testPostupci.getBroj()).isEqualTo(UPDATED_BROJ);
        assertThat(testPostupci.getIme()).isEqualTo(UPDATED_IME);
    }

    @Test
    @Transactional
    void putNonExistingPostupci() throws Exception {
        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();
        postupci.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPostupciMockMvc
            .perform(
                put(ENTITY_API_URL_ID, postupci.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(postupci))
            )
            .andExpect(status().isBadRequest());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPostupci() throws Exception {
        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();
        postupci.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostupciMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(postupci))
            )
            .andExpect(status().isBadRequest());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPostupci() throws Exception {
        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();
        postupci.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostupciMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(postupci)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePostupciWithPatch() throws Exception {
        // Initialize the database
        postupciRepository.saveAndFlush(postupci);

        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();

        // Update the postupci using partial update
        Postupci partialUpdatedPostupci = new Postupci();
        partialUpdatedPostupci.setId(postupci.getId());

        partialUpdatedPostupci.broj(UPDATED_BROJ).ime(UPDATED_IME);

        restPostupciMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPostupci.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPostupci))
            )
            .andExpect(status().isOk());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
        Postupci testPostupci = postupciList.get(postupciList.size() - 1);
        assertThat(testPostupci.getBroj()).isEqualTo(UPDATED_BROJ);
        assertThat(testPostupci.getIme()).isEqualTo(UPDATED_IME);
    }

    @Test
    @Transactional
    void fullUpdatePostupciWithPatch() throws Exception {
        // Initialize the database
        postupciRepository.saveAndFlush(postupci);

        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();

        // Update the postupci using partial update
        Postupci partialUpdatedPostupci = new Postupci();
        partialUpdatedPostupci.setId(postupci.getId());

        partialUpdatedPostupci.broj(UPDATED_BROJ).ime(UPDATED_IME);

        restPostupciMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPostupci.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPostupci))
            )
            .andExpect(status().isOk());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
        Postupci testPostupci = postupciList.get(postupciList.size() - 1);
        assertThat(testPostupci.getBroj()).isEqualTo(UPDATED_BROJ);
        assertThat(testPostupci.getIme()).isEqualTo(UPDATED_IME);
    }

    @Test
    @Transactional
    void patchNonExistingPostupci() throws Exception {
        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();
        postupci.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPostupciMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, postupci.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(postupci))
            )
            .andExpect(status().isBadRequest());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPostupci() throws Exception {
        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();
        postupci.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostupciMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(postupci))
            )
            .andExpect(status().isBadRequest());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPostupci() throws Exception {
        int databaseSizeBeforeUpdate = postupciRepository.findAll().size();
        postupci.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostupciMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(postupci)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Postupci in the database
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePostupci() throws Exception {
        // Initialize the database
        postupciRepository.saveAndFlush(postupci);

        int databaseSizeBeforeDelete = postupciRepository.findAll().size();

        // Delete the postupci
        restPostupciMockMvc
            .perform(delete(ENTITY_API_URL_ID, postupci.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Postupci> postupciList = postupciRepository.findAll();
        assertThat(postupciList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
