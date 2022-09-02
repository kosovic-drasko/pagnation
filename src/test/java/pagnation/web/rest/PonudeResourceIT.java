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
import pagnation.domain.Ponude;
import pagnation.repository.PonudeRepository;
import pagnation.service.criteria.PonudeCriteria;

/**
 * Integration tests for the {@link PonudeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PonudeResourceIT {

    private static final Integer DEFAULT_BROJ = 1;
    private static final Integer UPDATED_BROJ = 2;
    private static final Integer SMALLER_BROJ = 1 - 1;

    private static final String DEFAULT_IME = "AAAAAAAAAA";
    private static final String UPDATED_IME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/ponudes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PonudeRepository ponudeRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPonudeMockMvc;

    private Ponude ponude;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ponude createEntity(EntityManager em) {
        Ponude ponude = new Ponude().broj(DEFAULT_BROJ).ime(DEFAULT_IME);
        return ponude;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ponude createUpdatedEntity(EntityManager em) {
        Ponude ponude = new Ponude().broj(UPDATED_BROJ).ime(UPDATED_IME);
        return ponude;
    }

    @BeforeEach
    public void initTest() {
        ponude = createEntity(em);
    }

    @Test
    @Transactional
    void createPonude() throws Exception {
        int databaseSizeBeforeCreate = ponudeRepository.findAll().size();
        // Create the Ponude
        restPonudeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(ponude)))
            .andExpect(status().isCreated());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeCreate + 1);
        Ponude testPonude = ponudeList.get(ponudeList.size() - 1);
        assertThat(testPonude.getBroj()).isEqualTo(DEFAULT_BROJ);
        assertThat(testPonude.getIme()).isEqualTo(DEFAULT_IME);
    }

    @Test
    @Transactional
    void createPonudeWithExistingId() throws Exception {
        // Create the Ponude with an existing ID
        ponude.setId(1L);

        int databaseSizeBeforeCreate = ponudeRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPonudeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(ponude)))
            .andExpect(status().isBadRequest());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllPonudes() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList
        restPonudeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ponude.getId().intValue())))
            .andExpect(jsonPath("$.[*].broj").value(hasItem(DEFAULT_BROJ)))
            .andExpect(jsonPath("$.[*].ime").value(hasItem(DEFAULT_IME)));
    }

    @Test
    @Transactional
    void getPonude() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get the ponude
        restPonudeMockMvc
            .perform(get(ENTITY_API_URL_ID, ponude.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ponude.getId().intValue()))
            .andExpect(jsonPath("$.broj").value(DEFAULT_BROJ))
            .andExpect(jsonPath("$.ime").value(DEFAULT_IME));
    }

    @Test
    @Transactional
    void getPonudesByIdFiltering() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        Long id = ponude.getId();

        defaultPonudeShouldBeFound("id.equals=" + id);
        defaultPonudeShouldNotBeFound("id.notEquals=" + id);

        defaultPonudeShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultPonudeShouldNotBeFound("id.greaterThan=" + id);

        defaultPonudeShouldBeFound("id.lessThanOrEqual=" + id);
        defaultPonudeShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllPonudesByBrojIsEqualToSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where broj equals to DEFAULT_BROJ
        defaultPonudeShouldBeFound("broj.equals=" + DEFAULT_BROJ);

        // Get all the ponudeList where broj equals to UPDATED_BROJ
        defaultPonudeShouldNotBeFound("broj.equals=" + UPDATED_BROJ);
    }

    @Test
    @Transactional
    void getAllPonudesByBrojIsNotEqualToSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where broj not equals to DEFAULT_BROJ
        defaultPonudeShouldNotBeFound("broj.notEquals=" + DEFAULT_BROJ);

        // Get all the ponudeList where broj not equals to UPDATED_BROJ
        defaultPonudeShouldBeFound("broj.notEquals=" + UPDATED_BROJ);
    }

    @Test
    @Transactional
    void getAllPonudesByBrojIsInShouldWork() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where broj in DEFAULT_BROJ or UPDATED_BROJ
        defaultPonudeShouldBeFound("broj.in=" + DEFAULT_BROJ + "," + UPDATED_BROJ);

        // Get all the ponudeList where broj equals to UPDATED_BROJ
        defaultPonudeShouldNotBeFound("broj.in=" + UPDATED_BROJ);
    }

    @Test
    @Transactional
    void getAllPonudesByBrojIsNullOrNotNull() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where broj is not null
        defaultPonudeShouldBeFound("broj.specified=true");

        // Get all the ponudeList where broj is null
        defaultPonudeShouldNotBeFound("broj.specified=false");
    }

    @Test
    @Transactional
    void getAllPonudesByBrojIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where broj is greater than or equal to DEFAULT_BROJ
        defaultPonudeShouldBeFound("broj.greaterThanOrEqual=" + DEFAULT_BROJ);

        // Get all the ponudeList where broj is greater than or equal to UPDATED_BROJ
        defaultPonudeShouldNotBeFound("broj.greaterThanOrEqual=" + UPDATED_BROJ);
    }

    @Test
    @Transactional
    void getAllPonudesByBrojIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where broj is less than or equal to DEFAULT_BROJ
        defaultPonudeShouldBeFound("broj.lessThanOrEqual=" + DEFAULT_BROJ);

        // Get all the ponudeList where broj is less than or equal to SMALLER_BROJ
        defaultPonudeShouldNotBeFound("broj.lessThanOrEqual=" + SMALLER_BROJ);
    }

    @Test
    @Transactional
    void getAllPonudesByBrojIsLessThanSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where broj is less than DEFAULT_BROJ
        defaultPonudeShouldNotBeFound("broj.lessThan=" + DEFAULT_BROJ);

        // Get all the ponudeList where broj is less than UPDATED_BROJ
        defaultPonudeShouldBeFound("broj.lessThan=" + UPDATED_BROJ);
    }

    @Test
    @Transactional
    void getAllPonudesByBrojIsGreaterThanSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where broj is greater than DEFAULT_BROJ
        defaultPonudeShouldNotBeFound("broj.greaterThan=" + DEFAULT_BROJ);

        // Get all the ponudeList where broj is greater than SMALLER_BROJ
        defaultPonudeShouldBeFound("broj.greaterThan=" + SMALLER_BROJ);
    }

    @Test
    @Transactional
    void getAllPonudesByImeIsEqualToSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where ime equals to DEFAULT_IME
        defaultPonudeShouldBeFound("ime.equals=" + DEFAULT_IME);

        // Get all the ponudeList where ime equals to UPDATED_IME
        defaultPonudeShouldNotBeFound("ime.equals=" + UPDATED_IME);
    }

    @Test
    @Transactional
    void getAllPonudesByImeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where ime not equals to DEFAULT_IME
        defaultPonudeShouldNotBeFound("ime.notEquals=" + DEFAULT_IME);

        // Get all the ponudeList where ime not equals to UPDATED_IME
        defaultPonudeShouldBeFound("ime.notEquals=" + UPDATED_IME);
    }

    @Test
    @Transactional
    void getAllPonudesByImeIsInShouldWork() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where ime in DEFAULT_IME or UPDATED_IME
        defaultPonudeShouldBeFound("ime.in=" + DEFAULT_IME + "," + UPDATED_IME);

        // Get all the ponudeList where ime equals to UPDATED_IME
        defaultPonudeShouldNotBeFound("ime.in=" + UPDATED_IME);
    }

    @Test
    @Transactional
    void getAllPonudesByImeIsNullOrNotNull() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where ime is not null
        defaultPonudeShouldBeFound("ime.specified=true");

        // Get all the ponudeList where ime is null
        defaultPonudeShouldNotBeFound("ime.specified=false");
    }

    @Test
    @Transactional
    void getAllPonudesByImeContainsSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where ime contains DEFAULT_IME
        defaultPonudeShouldBeFound("ime.contains=" + DEFAULT_IME);

        // Get all the ponudeList where ime contains UPDATED_IME
        defaultPonudeShouldNotBeFound("ime.contains=" + UPDATED_IME);
    }

    @Test
    @Transactional
    void getAllPonudesByImeNotContainsSomething() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        // Get all the ponudeList where ime does not contain DEFAULT_IME
        defaultPonudeShouldNotBeFound("ime.doesNotContain=" + DEFAULT_IME);

        // Get all the ponudeList where ime does not contain UPDATED_IME
        defaultPonudeShouldBeFound("ime.doesNotContain=" + UPDATED_IME);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPonudeShouldBeFound(String filter) throws Exception {
        restPonudeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ponude.getId().intValue())))
            .andExpect(jsonPath("$.[*].broj").value(hasItem(DEFAULT_BROJ)))
            .andExpect(jsonPath("$.[*].ime").value(hasItem(DEFAULT_IME)));

        // Check, that the count call also returns 1
        restPonudeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPonudeShouldNotBeFound(String filter) throws Exception {
        restPonudeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPonudeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPonude() throws Exception {
        // Get the ponude
        restPonudeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPonude() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();

        // Update the ponude
        Ponude updatedPonude = ponudeRepository.findById(ponude.getId()).get();
        // Disconnect from session so that the updates on updatedPonude are not directly saved in db
        em.detach(updatedPonude);
        updatedPonude.broj(UPDATED_BROJ).ime(UPDATED_IME);

        restPonudeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPonude.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPonude))
            )
            .andExpect(status().isOk());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
        Ponude testPonude = ponudeList.get(ponudeList.size() - 1);
        assertThat(testPonude.getBroj()).isEqualTo(UPDATED_BROJ);
        assertThat(testPonude.getIme()).isEqualTo(UPDATED_IME);
    }

    @Test
    @Transactional
    void putNonExistingPonude() throws Exception {
        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();
        ponude.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPonudeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ponude.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(ponude))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPonude() throws Exception {
        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();
        ponude.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPonudeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(ponude))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPonude() throws Exception {
        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();
        ponude.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPonudeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(ponude)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePonudeWithPatch() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();

        // Update the ponude using partial update
        Ponude partialUpdatedPonude = new Ponude();
        partialUpdatedPonude.setId(ponude.getId());

        partialUpdatedPonude.broj(UPDATED_BROJ);

        restPonudeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPonude.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPonude))
            )
            .andExpect(status().isOk());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
        Ponude testPonude = ponudeList.get(ponudeList.size() - 1);
        assertThat(testPonude.getBroj()).isEqualTo(UPDATED_BROJ);
        assertThat(testPonude.getIme()).isEqualTo(DEFAULT_IME);
    }

    @Test
    @Transactional
    void fullUpdatePonudeWithPatch() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();

        // Update the ponude using partial update
        Ponude partialUpdatedPonude = new Ponude();
        partialUpdatedPonude.setId(ponude.getId());

        partialUpdatedPonude.broj(UPDATED_BROJ).ime(UPDATED_IME);

        restPonudeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPonude.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPonude))
            )
            .andExpect(status().isOk());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
        Ponude testPonude = ponudeList.get(ponudeList.size() - 1);
        assertThat(testPonude.getBroj()).isEqualTo(UPDATED_BROJ);
        assertThat(testPonude.getIme()).isEqualTo(UPDATED_IME);
    }

    @Test
    @Transactional
    void patchNonExistingPonude() throws Exception {
        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();
        ponude.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPonudeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, ponude.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(ponude))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPonude() throws Exception {
        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();
        ponude.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPonudeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(ponude))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPonude() throws Exception {
        int databaseSizeBeforeUpdate = ponudeRepository.findAll().size();
        ponude.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPonudeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(ponude)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ponude in the database
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePonude() throws Exception {
        // Initialize the database
        ponudeRepository.saveAndFlush(ponude);

        int databaseSizeBeforeDelete = ponudeRepository.findAll().size();

        // Delete the ponude
        restPonudeMockMvc
            .perform(delete(ENTITY_API_URL_ID, ponude.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Ponude> ponudeList = ponudeRepository.findAll();
        assertThat(ponudeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
